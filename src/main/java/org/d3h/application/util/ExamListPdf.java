package org.d3h.application.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.d3h.application.models.Exam;
import org.d3h.application.models.ExamStudent;
import org.springframework.core.io.ClassPathResource;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ExamListPdf {
//Import font MOD20 et MAGNETO 
public static final String MOD20 = new ClassPathResource("static/fonts/MOD20.TTF").getPath();
public static final String NEWLINE = "\n";
public static final ClassPathResource LOGO = new ClassPathResource("static/images/logo_uiz.png");

public  static ByteArrayInputStream create(Exam exam){
	
	try {		
			ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4,20, 20, 90, 0);

            Image image= null;
			try {
				
				image = Image.getInstance("src/main/resources/static/images/logo_uiz.png");
                image.scaleAbsolute(80,80);
                float x = (PageSize.A4.getWidth() - image.getScaledWidth()) / 2;
                float y = (PageSize.A4.getHeight()- image.getScaledWidth());
                image.setAbsolutePosition(x, y);

			} catch (IOException e) {
				e.printStackTrace();
			}
					                
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
           
            FontSelector fs = new FontSelector();
			Font font = FontFactory.getFont(MOD20, 14,Font.NORMAL);
			fs.addFont(font);
			Phrase title = fs.process(
				"Examen N° "+exam.getId()+NEWLINE
				+"Date : "+exam.getDate().toString()+NEWLINE
				+"Module : "+exam.getModule().getLabel()+NEWLINE
				+"Session : "+exam.getSession()+"   |    "
				+"Semestre : "+exam.getModule().getSemestre()+NEWLINE
				+"Salle N° "+exam.getRoom().getNumero()+"   |    "
				+"Département : "+exam.getRoom().getDepartement()+NEWLINE
			);
            
            // Adding cells to the table       
            table.addCell(getCell_MOD20_bold("NOM CPMPLET"));       
            
            table.addCell(getCell_MOD20_bold("APOGEE"));       
            
            table.addCell(getCell_MOD20_bold("CNE"));
            
            table.addCell(getCell_MOD20_bold("PRESENCE"));
            
            for(ExamStudent es : exam.getStudents()) {
            	
            	String fullName = es.getStudent().getNom().toUpperCase()+" "+es.getStudent().getPrenom();
            	String presence = es.isPresent()?"P":"ABI";
            	
            	table.addCell(getCell_MOD20(fullName));       
                
                table.addCell(getCell_MOD20(es.getStudent().getApogee()));       
                
                table.addCell(getCell_MOD20(es.getStudent().getCne()));
              
                table.addCell(getCell_MOD20(presence));
            }
            	
            //-----------------
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Phrase("\n "));
            document.add(image);
            document.add(title);
            document.add(new Phrase("\n "));
            document.add(table);
            
            document.close();
            //file.close();
            
    		return new ByteArrayInputStream(out.toByteArray());
        } catch (DocumentException e) {
        	
        	System.out.println(e);
        	return null;
        }
	}
		    
    //---------------------------------------------------------------------------------
    public static PdfPCell getCell_magneto(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(MOD20, 10);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorder(0);
		return cell;
    }
    public static PdfPCell getCell_magneto_center(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(MOD20, 10);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);	
        cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		//cell.setBorder(0);
		
		return cell;
    }
    public static PdfPCell getCell_MOD20(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(MOD20, 14);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorderWidth(1);
		cell.setPadding(5);
		return cell;
    }
    public static PdfPCell getCell_MOD20_bold(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(MOD20, 14,Font.BOLD);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorderWidth(1);
		cell.setPadding(5);
		cell.setBackgroundColor(new BaseColor(218, 225, 231));
		return cell;
    }
}