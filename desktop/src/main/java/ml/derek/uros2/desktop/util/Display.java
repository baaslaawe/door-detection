package ml.derek.uros2.desktop.util;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Display
{
    public static JFrame image(BufferedImage... imgs)
    {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        //JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 20);
        //frame.getContentPane().add(slider);

        for(BufferedImage image : imgs)
        {
            JLabel content = new JLabel(new ImageIcon(image));
            content.setSize(100, 100);
            frame.getContentPane().add(content);
        }

        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

    public static JFrame image(Mat... imgs)
    {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        //JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 20);
        //frame.getContentPane().add(slider);

        for(Mat image : imgs)
        {
            JLabel content = new JLabel(new ImageIcon(Convert.bufferedImage(image)));
            content.setSize(100, 100);
            frame.getContentPane().add(content);
        }

        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

}
