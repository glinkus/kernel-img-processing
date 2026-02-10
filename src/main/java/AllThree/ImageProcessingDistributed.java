package AllThree;

import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageProcessingDistributed {
    public static final float[][] DEFAULT_KERNEL = {
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}
    };
    /*public static final float[][] DEFAULT_KERNEL = {
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 0}
    };*/
    public void processImage(String inputImagePath, String outputImagePath) throws Exception {
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        byte[] imageData = null;
        BufferedImage image = null;

        if (rank == 0) {
            // Load and convert the image on the master node before distributing
            BufferedImage originalImage = Imaging.getBufferedImage(new File(inputImagePath));
            image = convertToARGB(originalImage);  // Convert to ARGB format

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);  // Write the ARGB image to byte array
            imageData = baos.toByteArray();
            System.out.println(imageData.length);
        }

        // Broadcasting the size of the image data array
        int[] imageSize = new int[1];
        if (rank == 0) {
            imageSize[0] = imageData.length;
        }
        MPI.COMM_WORLD.Bcast(imageSize, 0, 1, MPI.INT, 0);

        // Prepare the buffer to receive image data
        if (rank != 0) {
            imageData = new byte[imageSize[0]];
        }
        MPI.COMM_WORLD.Bcast(imageData, 0, imageSize[0], MPI.BYTE, 0);

        // Deserialize the image data
        if (rank != 0) {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            image = ImageIO.read(bais);
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                image = convertToARGB(image); // Convert if not already in expected format
            }
        }
        System.out.println("Post-conversion image type at rank " + rank + ": " + image.getType());

        int width = image.getWidth();
        int height = image.getHeight();
        int segmentHeight = height / size;
        int startRow = rank * segmentHeight;
        int endRow = (rank == size - 1) ? height : (startRow + segmentHeight);

        BufferedImage segmentImage = processSegment(image, DEFAULT_KERNEL, startRow, endRow, width, height);
        // Convert BufferedImage to array
        int[] buffer = ((DataBufferInt) segmentImage.getRaster().getDataBuffer()).getData();
        int[] recvBuffer = null;
        if (rank == 0) {
            recvBuffer = new int[width * height];
        }

        int[] segmentSizes = new int[size];
        int[] displacements = new int[size];
        for (int i = 0; i < size; i++) {
            segmentSizes[i] = width * (i == size - 1 ? height - (i * segmentHeight) : segmentHeight);
            displacements[i] = i * segmentHeight * width;
        }

        MPI.COMM_WORLD.Gatherv(
                buffer, 0, buffer.length, MPI.INT,
                recvBuffer, 0, segmentSizes, displacements, MPI.INT,
                0
        );

        if (rank == 0) {
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            outputImage.getRaster().setDataElements(0, 0, width, height, recvBuffer);

            File outputFile = new File(outputImagePath);
            Imaging.writeImage(outputImage, outputFile, ImageFormats.PNG);
            System.out.println("Distributed image processing complete. Output saved to: " + outputImagePath);
        }
    }
    private BufferedImage convertToARGB(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            System.out.println("Image converted to TYPE_INT_ARGB format.");
            return newImage;
        }
        System.out.println("Image already in TYPE_INT_ARGB format.");
        return image;
    }

    private BufferedImage processSegment(BufferedImage originalImage, float[][] kernel, int startRow, int endRow, int width, int height) {
        // Ensure the segment image is the correct type
        System.out.println("Segment image type before processing: " + originalImage.getType());
        BufferedImage segmentImage = new BufferedImage(width, endRow - startRow, BufferedImage.TYPE_INT_ARGB);
        if (originalImage.getRaster().getDataBuffer() instanceof DataBufferInt) {
            DataBufferInt buffer = (DataBufferInt) originalImage.getRaster().getDataBuffer();
            int[] data = buffer.getData();
            System.out.println("Expected data buffer type.");
        } else {
            System.out.println("Unexpected data buffer type.");
            // Handle error or convert image
        }
        if (!(originalImage.getRaster().getDataBuffer() instanceof DataBufferInt)) {
            throw new IllegalArgumentException("Image buffer type is not compatible: " + originalImage.getRaster().getDataBuffer().getClass());
        }
        //BufferedImage segmentImage = new BufferedImage(width, endRow - startRow, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt buffer = (DataBufferInt) originalImage.getRaster().getDataBuffer();
        int[] data = buffer.getData(); // Access the underlying data array directly

        int[] segmentData = ((DataBufferInt) segmentImage.getRaster().getDataBuffer()).getData();

        int kernelWidth = kernel.length;
        int kernelHeight = kernel[0].length;
        int kernelHalfWidth = kernelWidth / 2;
        int kernelHalfHeight = kernelHeight / 2;

        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < width; x++) {
                float red = 0, green = 0, blue = 0;
                for (int ky = -kernelHalfHeight; ky <= kernelHalfHeight; ky++) {
                    int pixelY = y + ky;
                    if (pixelY < 0 || pixelY >= height) continue;

                    for (int kx = -kernelHalfWidth; kx <= kernelHalfWidth; kx++) {
                        int pixelX = x + kx;
                        if (pixelX < 0 || pixelX >= width) continue;

                        int pixelColor = data[pixelY * width + pixelX];
                        float kernelValue = kernel[ky + kernelHalfHeight][kx + kernelHalfWidth];
                        red += ((pixelColor >> 16) & 0xFF) * kernelValue;
                        green += ((pixelColor >> 8) & 0xFF) * kernelValue;
                        blue += (pixelColor & 0xFF) * kernelValue;
                    }
                }
                int newColor = (Math.min(Math.max((int)red, 0), 255) << 16) |
                        (Math.min(Math.max((int)green, 0), 255) << 8) |
                        Math.min(Math.max((int)blue, 0), 255);
                segmentData[(y - startRow) * width + x] = newColor | 0xFF000000; // Set alpha to 255
            }
        }
        return segmentImage;
    }

    public static void main(String[] args) {
        try {
            MPI.Init(args);
            String inputImagePath = "C:\\Users\\Gustas\\Desktop\\Concurrent\\Animal2.png";
            String outputImagePath = "C:\\Users\\Gustas\\Desktop\\Concurrent\\ResultDis.png";
            long startTime = System.currentTimeMillis();

            ImageProcessingDistributed ipd = new ImageProcessingDistributed();
            ipd.processImage(inputImagePath, outputImagePath);
            MPI.Finalize();
            long endTime = System.currentTimeMillis();
            System.out.println("Time taken to process image: " + (endTime - startTime) + " milliseconds");

        } catch (Exception e) {
                e.printStackTrace();
            }

    }
}
