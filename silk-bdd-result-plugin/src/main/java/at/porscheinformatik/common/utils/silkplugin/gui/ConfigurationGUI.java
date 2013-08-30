package at.porscheinformatik.common.utils.silkplugin.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.IOUtils;

/**
 * @author Stefan Mayer (yms)
 */
public class ConfigurationGUI extends JFrame
{
    private static final long serialVersionUID = 2474865722690481411L;

    private JTextField textFieldDbName;
    private JTextField textFieldPort;
    private JTextField textFieldHost;
    private JTextField textObsoletetLimit;
    private JTextField textExecIdFormat;
    private JTextField textCollectionNamingConvention;
    private JTextField textBddReport;
    private JButton buttonSave;

    private Properties config = new Properties();

    public static void main(String[] args)
    {
        try
        {
            new ConfigurationGUI();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ConfigurationGUI() throws Exception
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.setTitle("Silk Central BDD Result Plugin Configurator");
        this.setSize(400, 495);
        this.setLocation(new Point(400, 200));

        Container content = this.getContentPane();
        content.setLayout(null);

        loadProperties();

        createGuiComponents(content);

        buttonSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    config.setProperty("database.host", textFieldHost.getText());
                    config.setProperty("database.port", textFieldPort.getText());
                    config.setProperty("database.name", textFieldDbName.getText());

                    config.setProperty("report.obsolete-limit", textObsoletetLimit.getText());
                    config.setProperty("silk.execution-id-name", textExecIdFormat.getText());
                    config.setProperty("collection.naming-convention", textCollectionNamingConvention.getText());
                    config.setProperty("bdd.report", textBddReport.getText());

                    saveProperties();

                    JOptionPane.showMessageDialog(ConfigurationGUI.this, "Configuration saved!", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                    System.exit(1);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        });
        this.setVisible(true);
    }

    private void createGuiComponents(Container container) throws Exception
    {
        int x = 15;
        int y = 120;

        BufferedImage logo = ImageIO.read(getClass().getResourceAsStream("/poi.jpg"));
        JLabel labelLogo = new JLabel(new ImageIcon(logo), SwingConstants.CENTER);
        labelLogo.setBackground(Color.WHITE);
        labelLogo.setOpaque(true);
        labelLogo.setBounds(0, 0, 400, 100);
        labelLogo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        container.add(labelLogo);

        //----------------------------------------------------------------
        JPanel panelDb = new JPanel(new GridLayout(3, 2, 0, 5));
        panelDb.setBorder(new TitledBorder("Database Configurations:"));
        panelDb.setOpaque(false);
        panelDb.setBounds(x, y, 365, 120);

        JLabel labelHost = new JLabel("Host: ");
        panelDb.add(labelHost);
        textFieldHost = new JTextField(config.getProperty("database.host"));
        panelDb.add(textFieldHost);

        JLabel labelPort = new JLabel("Port: ");
        panelDb.add(labelPort);
        textFieldPort = new JTextField(config.getProperty("database.port"));
        panelDb.add(textFieldPort);

        JLabel labelDbName = new JLabel("Database name: ");
        panelDb.add(labelDbName);
        textFieldDbName = new JTextField(config.getProperty("database.name"));
        panelDb.add(textFieldDbName);

        //----------------------------------------------------------------
        JPanel panelOther = new JPanel(new GridLayout(4, 2, 0, 5));
        panelOther.setBorder(new TitledBorder("Other Configurations:"));
        panelOther.setOpaque(false);
        panelOther.setBounds(x, y + 130, 365, 155);

        JLabel labelObsoletetLimit = new JLabel("Obsolete Limit (Days): ");
        panelOther.add(labelObsoletetLimit);
        textObsoletetLimit = new JTextField(config.getProperty("report.obsolete-limit"));
        panelOther.add(textObsoletetLimit);

        JLabel labelExecIdFormat = new JLabel("ExecutionId format: ");
        panelOther.add(labelExecIdFormat);
        textExecIdFormat = new JTextField(config.getProperty("silk.execution-id-name"));
        panelOther.add(textExecIdFormat);

        JLabel labelCollectionNamingConvention = new JLabel("Collection Regex: ");
        panelOther.add(labelCollectionNamingConvention);
        textCollectionNamingConvention = new JTextField(config.getProperty("collection.naming-convention"));
        panelOther.add(textCollectionNamingConvention);

        JLabel labelBddReport = new JLabel("BDD-Report: ");
        panelOther.add(labelBddReport);
        textBddReport = new JTextField(config.getProperty("bdd.report"));
        panelOther.add(textBddReport);

        //----------------------------------------------------------------
        buttonSave = new JButton("Save");
        buttonSave.setBounds(x + 255, y + 300, 100, 25);
        container.add(buttonSave);

        container.add(panelDb);
        container.add(panelOther);

    }

    private void loadProperties()
    {
        try
        {
            InputStream in = new FileInputStream(new File(System.getProperty("user.home"), "config.properties"));
            config.load(in);
            in.close();
        }
        catch (IOException e)
        {
            try
            {
                OutputStream out = new FileOutputStream(new File(System.getProperty("user.home"), "config.properties"));
                IOUtils.copy(getClass().getResourceAsStream("/config.properties"), out);
                out.close();
                loadProperties();
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }

        }
    }

    private void saveProperties() throws IOException
    {
        FileOutputStream out = new FileOutputStream(new File(System.getProperty("user.home"), "config.properties"));
        config.store(out, null);
        out.close();
    }
}
