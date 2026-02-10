package AllThree;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageProcessing {
    public static final float[][] DEFAULT_KERNEL = {
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}
    };

    public static void processImage(String inputImagePath, String outputImagePath, boolean useParallelProcessing) throws Exception {
        BufferedImage image = Imaging.getBufferedImage(new File(inputImagePath));
        BufferedImage outputImage;

        if (useParallelProcessing) {
            outputImage = ImageProcessingParallel.processImageParallel(image, DEFAULT_KERNEL);
        } else {
            outputImage = ImageProcessingUtility.applyKernel(image, DEFAULT_KERNEL);
        }

        File outputFile = new File(outputImagePath);
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                System.err.println("Failed to delete existing output file: " + outputImagePath);
                return;
            }
        }
        Imaging.writeImage(outputImage, outputFile, ImageFormats.PNG);
        System.out.println("Image processing complete. Output saved to: " + outputImagePath);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageProcessingGUI gui = new ImageProcessingGUI();
            gui.setVisible(true);
        });
    }
}

