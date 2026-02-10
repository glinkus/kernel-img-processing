package AllThree;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessingGUI extends JFrame {

    private JTextField inputPathField;
    private JTextField outputPathField;
    private JLabel inputImageLabel;
    private JLabel outputImageLabel;
    private JCheckBox parallelProcessingCheckBox;
    private BufferedImage inputImage;
    private BufferedImage outputImage;

    public ImageProcessingGUI() {
        setTitle("Image Processing");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        // Input image selection
        JButton inputButton = new JButton("Select Input Image");
        inputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg"));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    inputPathField.setText(selectedFile.getAbsolutePath());
                    try {
                        inputImage = Imaging.getBufferedImage(selectedFile);
                        inputImageLabel.setIcon(new ImageIcon(inputImage.getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (ImageReadException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        inputPathField = new JTextField();
        panel.add(inputButton);
        panel.add(inputPathField);

        // Output path selection
        JButton outputButton = new JButton("Select Output Path");
        outputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    outputPathField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        outputPathField = new JTextField();
        panel.add(outputButton);
        panel.add(outputPathField);

        // Processing options
        parallelProcessingCheckBox = new JCheckBox("Parallel Processing");
        panel.add(parallelProcessingCheckBox);

        // Process button
        JButton processButton = new JButton("Process Image");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputPath = inputPathField.getText();
                String outputPath = outputPathField.getText();
                boolean useParallel = parallelProcessingCheckBox.isSelected();

                try {
                    ImageProcessing.processImage(inputPath, outputPath, useParallel);
                    outputImage = ImageIO.read(new File(outputPath));
                    outputImageLabel.setIcon(new ImageIcon(outputImage.getScaledInstance(300, 200, Image.SCALE_SMOOTH)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        panel.add(processButton);

        add(panel, BorderLayout.NORTH);

        // Image display panel
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(1, 2));
        inputImageLabel = new JLabel();
        outputImageLabel = new JLabel();
        imagePanel.add(inputImageLabel);
        imagePanel.add(outputImageLabel);
        add(imagePanel, BorderLayout.CENTER);
    }
}

