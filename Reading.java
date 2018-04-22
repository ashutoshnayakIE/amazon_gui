package orders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

public class Reading {
    
    public int min(int a, int b){
        if (a < b){
            return (a);
        } else {
            return (b);
        }
    }
    
    public static String[] lines;                              // contains the data in the file
    public ArrayList<String> address_text = new ArrayList<>(); // contains the address of the seller entered in the txt file
    
    public void FileInfo() throws IOException{
        /*
        This function is used to read the file downloaded from amazon seller central
        */
        File input_file = Interface.data_file; 
        PDDocument document = PDDocument.load(input_file);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        lines = text.split(System.getProperty("line.separator")); 
        document.close();
        
        // reading the address file
        address_text.add("From");
        BufferedReader br = new BufferedReader(new FileReader("./address.txt"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                address_text.add(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        // adding some blank text randomly so that the it can be safe to print
        for (int y =0 ; y < 5; y++){
            address_text.add("");
        }
    }
    
    public void WriteInfo () throws IOException{
        /*
        this function writes the data in the PDF file where you want it to be saved
        */
        PDDocument document_write = new PDDocument();
        
        PDPage page = new PDPage(PDRectangle.A4);
        PDPageContentStream content = new PDPageContentStream(document_write, page);
        content.beginText();
        content.setLeading(14.5f);
        int col = 400;
        /*
        - create an offset to start te document at the top of the page
        - otherwise it starts at the bottom left of the page
        - col stores the variable value for the second column for seller address
        */
        content.newLineAtOffset(col+10, 820);
        
        int doc_length = lines.length;
        ArrayList<String> order_id = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        
        int l = 0;
        int count_lines = 0; // counts the total lines in the document so far
        
        while (l < doc_length){
            String line_dummy = lines[l].toLowerCase();
            
            if (line_dummy.contains("ship to:")){
                
                // add a new page in the document after placing 6 orders in one page
                if (order_id.size()%6 == 0 & order_id.size() > 0){
                    content.endText();
                    content.close();
                    document_write.addPage(page);
                    
                    page = new PDPage(PDRectangle.A4);
                    content = new PDPageContentStream(document_write, page);
                    content.beginText();
                    content.newLineAtOffset(col+10, 820);
                }
                /*
                writing "Ship To" and "From" in different font and size 
                */
                int count_order_lines = 0;
                content.setFont(PDType1Font.HELVETICA_BOLD, 20);
                content.newLineAtOffset(-col, 0);
                content.showText(lines[l]);
                content.newLineAtOffset(col, 0);
                content.showText(address_text.get(count_order_lines));
                content.setLeading(10.5f);
                content.newLine();
                content.newLine();
                content.setLeading(14.5f);
                count_lines += 1;
                System.out.println(lines[l]);
                l += 1;
                
                // continue witing the file till the address of the user is being read
                while (true){
                    
                    content.setFont(PDType1Font.HELVETICA, 14);
                    content.newLineAtOffset(-col, 0);
                    content.showText(lines[l]);
                    content.newLineAtOffset(col, 0);
                    content.showText(address_text.get(count_order_lines+1));
                    if (count_order_lines == 1){
                        name.add(lines[l]);
                    }
                    
                    content.newLine();
                    count_lines += 1;
                    count_order_lines += 1;
                    System.out.println(lines[l]);
                    
                    // if the user address is finished, move to the next line until a new order is observed
                    if (lines[l].toLowerCase().contains("order id")){
                        order_id.add(lines[l].replace("Order ID:", ""));
                        System.out.println("-------------------");
                        count_lines += 1;
                        break;
                    }
                    l += 1;
                }
                content.newLine();
            }
            l += 1;
        }
        
        /*
        Add another page to write all the order ids that could be used 
        this could be used to noting down the tracking id when you send the product via postal service
        this part of the file stores 25 order Ids in one page
        */
        
        for (int oo = 0; oo < order_id.size()/25+1;oo++){
            page = new PDPage(PDRectangle.A4);
            content = new PDPageContentStream(document_write, page);
            content.beginText();
            content.newLineAtOffset(10, 800);
            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.showText("Order Ids");
            content.newLineAtOffset(col, 0);
            content.showText("Tracking Number");
            content.setLeading(28.5f);
            content.newLine();

            content.setFont(PDType1Font.HELVETICA, 14);
            content.newLineAtOffset(-col, 0);

            for (int o = oo*25; o < min(order_id.size(),25+oo*25); o++){
                content.showText(order_id.get(o)+" --- " + name.get(o));
                content.newLine();
            }
            content.endText();
            content.close();
            document_write.addPage(page);
        }
        
        document_write.save(Interface.output_location);
        document_write.close();
        System.out.println(order_id.size());
        
    }
    
}
