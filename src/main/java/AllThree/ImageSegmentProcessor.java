package AllThree;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class ImageSegmentProcessor implements Callable<BufferedImage> {
    private BufferedImage originalImage;
    private float[][] kernel;
    private int startRow, endRow;

    public ImageSegmentProcessor(BufferedImage originalImage, float[][] kernel, int startRow, int endRow) {
        this.originalImage = originalImage;
        this.kernel = kernel;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public BufferedImage call() throws Exception {
        int width = originalImage.getWidth();
        int kernelWidth = kernel.length;
        int kernelHeight = kernel[0].length;

        BufferedImage segmentImage = new BufferedImage(width, endRow - startRow, originalImage.getType());

        for (int y = startRow; y < endRow; y++) {
            for (int x = kernelWidth / 2; x < width - kernelWidth / 2; x++) {
                float red = 0.0f, green = 0.0f, blue = 0.0f;

                for (int i = 0; i < kernelHeight; i++) {
                    for (int j = 0; j < kernelWidth; j++) {
                        int pixelX = x + j - kernelWidth / 2;
                        int pixelY = y + i - kernelHeight / 2;

                        // Check for boundary conditions
                        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= originalImage.getHeight()) {
                            continue;
                        }

                        int rgb = originalImage.getRGB(pixelX, pixelY);

                        red += ((rgb >> 16) & 0xff) * kernel[i][j];
                        green += ((rgb >> 8) & 0xff) * kernel[i][j];
                        blue += (rgb & 0xff) * kernel[i][j];
                    }
                }

                int newRed = Math.min(Math.max((int) red, 0), 255);
                int newGreen = Math.min(Math.max((int) green, 0), 255);
                int newBlue = Math.min(Math.max((int) blue, 0), 255);

                int newRGB = (newRed << 16) | (newGreen << 8) | newBlue;
                segmentImage.setRGB(x, y - startRow, newRGB);
            }
        }

        return segmentImage;
    }

}
