package dovzhenko.andrew.nettyserver.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class TableBilder {
	private StringBuilder html = new StringBuilder();
	  
	public TableBilder addRowToTable(Object[] rowElements) {
	        html.append("<tr>");
	        Arrays.stream(rowElements).forEach((re) -> html.append("<td>").append(getObjectToString(re)).append("</td>"));
	        html.append("</tr>");
	        return this;
	}
	
	public void clean(){
		html.delete(0, html.length());
	}
	
	@Override
	public String toString() {
		return html.toString();
	}

	private String getObjectToString(Object object){
		String returnString = null;
		if(object == null){
			returnString = "empty";
		}else if(object instanceof Date){
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
			Date date = (Date)object;
			returnString = simpleDateFormat.format(date);
		}else {
			returnString = object.toString();
		}
		return returnString;
	}
}
