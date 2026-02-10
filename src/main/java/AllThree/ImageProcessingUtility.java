package AllThree;

import java.awt.image.BufferedImage;

public class ImageProcessingUtility {
    public static BufferedImage applyKernel(BufferedImage originalImage, float[][] kernel) {
        int kernelWidth = kernel.length;
        int kernelHeight = kernel[0].length;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage outputImage = new BufferedImage(width, height, originalImage.getType());

        for (int y = kernelHeight / 2; y < height - kernelHeight / 2; y++) {
            for (int x = kernelWidth / 2; x < width - kernelWidth / 2; x++) {
                float red = 0.0f, green = 0.0f, blue = 0.0f;

                for (int i = 0; i < kernelHeight; i++) {
                    for (int j = 0; j < kernelWidth; j++) {
                        int pixelX = x + j - kernelWidth / 2;
                        int pixelY = y + i - kernelHeight / 2;
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
                outputImage.setRGB(x, y, newRGB);
            }
        }

        return outputImage;
    }
}
