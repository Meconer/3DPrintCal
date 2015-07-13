/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Forms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Mats
 */
class GCodeFile {
    List<String> inList;
    List<String> outList;
    
    public void readFile() {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File("\\\\RASPBERRYPI\\public\\Filer\\3d-printer\\till utskrift"));
        int resultCode = jfc.showOpenDialog(null);
        
        if (resultCode == JFileChooser.APPROVE_OPTION ) {
            File file = jfc.getSelectedFile();
            try {
                inList = Files.readAllLines(file.toPath(), Charset.defaultCharset());
                System.out.println(inList.size());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Kan inte läsa filen" );
            }
        }
        
        
        
    }

    void saveFile() {
        JFileChooser jfc = new JFileChooser(new File("\\\\RASPBERRYPI\\public\\Filer\\3d-printer\\till utskrift"));
        int resultCode = jfc.showSaveDialog(null);
        
        if ( resultCode == JFileChooser.APPROVE_OPTION ) {
            FileWriter writer; 
            try {
                writer = new FileWriter( jfc.getSelectedFile());
                for(String str: outList) {
                    writer.write(str + "\n");
                }
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GCodeFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    
    public void calibrate() {
        final double radius = 80;
        final double zCorr = 1.3;
        double x = 0;
        double y = 0;
        double z = 0;
        double correction = 0;
        double origZ = 0;
        
        final String zRegex = "Z-?[\\d\\.]+";
        final String yRegex = "Y-?[\\d\\.]+";
        final String xRegex = "X-?[\\d\\.]+";
        Pattern zPattern = Pattern.compile(zRegex);
        Pattern yPattern = Pattern.compile(yRegex);
        Pattern xPattern = Pattern.compile(xRegex);
        
        DecimalFormat df = new DecimalFormat("#0.0#");
        
        
        
        Iterator<String> iter = inList.iterator();
        outList = new ArrayList<>();
        while (iter.hasNext()) {
            String line = iter.next();
            origZ = getCodeIfExist(line, origZ, "Z", zPattern );
            x = getCodeIfExist(line, x, "X", xPattern);
            y = getCodeIfExist(line, y, "Y", yPattern);
            System.out.println("X" + x + " Y" + y + " Z" + origZ);
            correction = getCorrection( x, y, radius, zCorr);
            z = origZ + correction;
            System.out.println("Ny Z: " + z);
            System.out.println("Före  : " + line);
            line = replaceZ(line, z, zPattern, df);
            System.out.println("Efter : " + line);
            outList.add(line);
        }
    }

    private double getCodeIfExist(String line, double origVal, String code, Pattern p) {
        double retVal = origVal;
        Matcher m = p.matcher(line);
        if ( m.find()) {
            retVal = Double.parseDouble(m.group().substring(1));
        }
        return retVal;
    }

    private double getCorrection(double x, double y, double radius, double zCorr) {
        double r = Math.sqrt( x*x + y*y );
        return zCorr * r / radius;
        
    }

    private String replaceZ(String line, double z, Pattern zPattern, DecimalFormat df) {
        String zValue = df.format(z);
        zValue = zValue.replace( ",", "." );
        if ( isMoveLine(line)) {
            Matcher m = zPattern.matcher( line );
            if ( m.find()) {
                line = line.substring(0, m.start()) + "Z" + zValue + line.substring(m.end(), line.length()) ;
            } else {
                line = line + " Z" + zValue;
            }
        }
        return line;
    }

    private boolean isMoveLine(String line) {
        if ( line.contains("X")) return true;
        if ( line.contains("Y")) return true;
        return line.contains("Z");
    }

    
}
