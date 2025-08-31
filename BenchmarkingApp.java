import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BenchmarkingApp extends JFrame {
    public BenchmarkingApp() {
        setTitle("System Benchmark Results");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(cpuBenchmark(), "Score", "CPU");
        dataset.addValue(multithreadingBenchmark(), "Score", "Multithreading");
        dataset.addValue(gpuBenchmark(), "Score", "GPU");
        dataset.addValue(memoryBenchmark(), "Score", "RAM");
        dataset.addValue(storageBenchmark(), "Score", "Storage");
        dataset.addValue(iopsBenchmark(), "Score", "IOPS");

        JFreeChart chart = ChartFactory.createBarChart(
                "Benchmark Scores", "Component", "Score",
                dataset, PlotOrientation.VERTICAL,
                false, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    private double cpuBenchmark() {
        long start = System.nanoTime();
        long result = 0;
        for (int i = 0; i < 1e7; i++) result += i * i;
        long end = System.nanoTime();
        return 1e9 / ((end - start) / 1e6); // Higher is better
    }

    private double multithreadingBenchmark() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        long start = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            executor.submit(() -> {
                long temp = 0;
                for (int j = 0; j < 1e6; j++) temp += j;
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return 0;
        }
        long end = System.nanoTime();
        return 1e9 / ((end - start) / 1e6);
    }

    private double gpuBenchmark() {
        long start = System.nanoTime();
        // Simulated GPU task (floating-point ops)
        float sum = 0f;
        for (int i = 0; i < 1e7; i++) {
            sum += Math.sin(i) * Math.cos(i);
        }
        long end = System.nanoTime();
        return 1e9 / ((end - start) / 1e6);
    }

    private double memoryBenchmark() {
        long start = System.nanoTime();
        int[] array = new int[10_000_000];
        for (int i = 0; i < array.length; i++) array[i] = i;
        long readStart = System.nanoTime();
        long sum = 0;
        for (int value : array) sum += value;
        long end = System.nanoTime();
        return 1e9 / ((end - start) / 1e6);
    }

    private double storageBenchmark() {
        try {
            File temp = File.createTempFile("benchmark", null);
            temp.deleteOnExit();
            long start = System.nanoTime();
            try (FileOutputStream out = new FileOutputStream(temp)) {
                byte[] data = new byte[1024 * 1024];
                for (int i = 0; i < 100; i++) {
                    out.write(data);
                }
            }
            long end = System.nanoTime();
            return 100.0 / ((end - start) / 1e9); // MB/sec
        } catch (Exception e) {
            return 0;
        }
    }

    private double iopsBenchmark() {
        try {
            File temp = File.createTempFile("iops", null);
            temp.deleteOnExit();
            RandomAccessFile raf = new RandomAccessFile(temp, "rw");
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                raf.seek((long) (Math.random() * 1024 * 1024));
                raf.write(1);
            }
            long end = System.nanoTime();
            raf.close();
            return 1000.0 / ((end - start) / 1e9); // ops/sec
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BenchmarkingApp app = new BenchmarkingApp();
            app.setVisible(true);
        });
    }
}
