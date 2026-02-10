package AllThree;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ImageProcessingParallel {
    public static BufferedImage processImageParallel(BufferedImage image, float[][] kernel) throws InterruptedException, ExecutionException {
        int cores = Runtime.getRuntime().availableProcessors();

        System.out.println("Cores: " + (cores));
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        List<Future<BufferedImage>> futures = new ArrayList<>();
        int height = image.getHeight();
        int segmentHeight = height / cores;

        for (int i = 0; i < cores; i++) {
            int startRow = i * segmentHeight;
            int endRow = (i == cores - 1) ? height : (i + 1) * segmentHeight;
            Callable<BufferedImage> task = new ImageSegmentProcessor(image, kernel, startRow, endRow);
            futures.add(executor.submit(task));
        }

        BufferedImage outputImage = new BufferedImage(image.getWidth(), height, image.getType());
        for (int i = 0; i < futures.size(); i++) {
            BufferedImage segment = futures.get(i).get();
            outputImage.getGraphics().drawImage(segment, 0, i * segmentHeight, null);
        }

        executor.shutdown();
        return outputImage;
    }
}
