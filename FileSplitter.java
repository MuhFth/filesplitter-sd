import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSplitter {

    private static final int MB = 1024 * 1024;

    public static void main(String[] args) {
        try {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            frame.setLocationRelativeTo(null);

            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.jpg;*.png", "jpg", "png");
            chooser.setFileFilter(filter);
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Select directory to save the file");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = chooser.getSelectedFile();
                System.out.println("Selected directory: " + selectedDirectory.getAbsolutePath());

                String urlString = JOptionPane.showInputDialog(frame, "Enter URL:");
                URL url = new URL(urlString);
                String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
                String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
                String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

                String newFileName = JOptionPane.showInputDialog(frame, "Enter new file name:", fileNameWithoutExtension);
                File outputFile = new File(selectedDirectory, newFileName + "." + fileExtension);

                System.out.println("Output file: " + outputFile.getAbsolutePath());

                String userInput = JOptionPane.showInputDialog(frame, "Enter max file size (in MB):", "5");
                long maxFileSize = Long.parseLong(userInput);

                downloadAndSaveFile(url, outputFile, maxFileSize);
                resizeAndSaveImage(outputFile, selectedDirectory, maxFileSize);

                JOptionPane.showMessageDialog(frame, "File and image download completed!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void downloadAndSaveFile(URL url, File outputFile, long maxFileSize) throws IOException {
        System.out.println("Downloading file...");

        try (java.io.InputStream inputStream = url.openStream()) {
            java.nio.file.Files.copy(inputStream, outputFile.toPath());
        }

        System.out.println("File downloaded.");

        long fileSize = outputFile.length();
        System.out.println("File size: " + fileSize + " bytes");

        if (fileSize > maxFileSize * MB) {
            outputFile.delete();
            throw new IOException("File size exceeds the limit.");
        }
    }

    private static void resizeAndSaveImage(File inputFile, File outputDirectory, long maxFileSize) throws IOException {
        System.out.println("Resizing image...");

        BufferedImage inputImage = ImageIO.read(inputFile);
        Dimension originalDimension = new Dimension(inputImage.getWidth(), inputImage.getHeight());
        Dimension maxDimension = new Dimension(1024, 768);
        double widthRatio = (double) maxDimension.width / originalDimension.width;
        double heightRatio = (double) maxDimension.height / originalDimension.height;
        double ratio = Math.min(widthRatio, heightRatio);
        int newWidth = (int) (originalDimension.width * ratio);
        int newHeight = (int) (originalDimension.height * ratio);

        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        System.out.println("Resized image.");

        File outputFile = new File(outputDirectory, inputFile.getName());
        ImageIO.write(outputImage, "jpg", outputFile);

        System.out.println("Image saved.");

        long fileSize = outputFile.length();
        System.out.println("File size: " + fileSize + " bytes");

        if (fileSize > maxFileSize * MB) {
            throw new IOException("File size exceeds the limit.");
        }
    }
}
