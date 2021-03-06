package controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.ServiceProvider;
import model.SysteemService;
import model.domain.Slb;

public class UrenOpenstellenServlet extends HttpServlet {
	private static final long serialVersionUID = -3241716947926761636L;
	private SysteemService sp = ServiceProvider.getService();

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String datum_req = req.getParameter("datum");
		String begintijd_req = req.getParameter("begintijd");
		String eindtijd_req = req.getParameter("eindtijd");
		String checkbox_split_req = req.getParameter("checkbox_split");
		String split_req = req.getParameter("split");
		String locatie_req = req.getParameter("locatie");
		
		req.setAttribute("datum",datum_req);
		req.setAttribute("begintijd",begintijd_req);
		req.setAttribute("eindtijd",eindtijd_req);
		req.setAttribute("split",split_req);
		req.setAttribute("locatie",locatie_req);
		
		if (checkbox_split_req != null) {
			req.setAttribute("checkbox_split","checked");
		}
			
		boolean leeg = false;
		if (datum_req == "" || datum_req == null) {
			leeg = true;
			req.setAttribute("errorDatum","Dit veld is verplicht!");
		}
		if (begintijd_req == "" || begintijd_req == null) {
			leeg = true;
			req.setAttribute("errorBegintijd","Dit veld is verplicht!");
		}	
		if (eindtijd_req == "" || eindtijd_req == null) {
			leeg = true;
			req.setAttribute("errorEindtijd","Dit veld is verplicht!");
		}
		if (leeg) {
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		//String 'datum' to Calendar 'datum'.
		Calendar datum = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			datum.setTime(sdf.parse(datum_req));
		} catch (ParseException e) {
			req.setAttribute("errorDatum","Fout formaat!");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		//String begintijd/eindtijd to Date 'datebegintijd/dateeindtijd'.
		SimpleDateFormat sdf_tijd = new SimpleDateFormat("HH:mm");
		sdf_tijd.setTimeZone(TimeZone.getTimeZone("GMT-5"));
		Date begintijd = null;
		try {
			begintijd = sdf_tijd.parse(begintijd_req);
		} catch (ParseException e) {
			req.setAttribute("errorBegintijd","Fout formaat!");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		Date eindtijd = null;
		try {
			eindtijd = sdf_tijd.parse(eindtijd_req);
		} catch (ParseException e) {
			req.setAttribute("errorEindtijd","Fout formaat!");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
				
		if (datum.before(Calendar.getInstance())) {
			//Error: Kan niet in het verleden urenopenstellen.
			req.setAttribute("errorDatum","Een datum in het verleden is niet toegestaan.");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		if (begintijd.after(eindtijd)) {
			//Error: Kan niet een kleinere eindtijd hebben.
			req.setAttribute("errorBegintijd","Begintijd moet voor eindtijd.");
			req.setAttribute("errorEindtijd","Eindtijd moet na begintijd.");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		int split = 0;
		if (checkbox_split_req != null) {
			//Is het wel een getal?
			try {
				split = Integer.parseInt(split_req);
			} catch (NumberFormatException e) {
				req.setAttribute("errorSplit","Invoer moet een (heel) getal zijn.");
				RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
				rd.forward(req, resp);
				return;
			}
			//Is het getal groter dan 720 (min)?
			if (split > 720) {
				req.setAttribute("errorSplit","Maximaal 12 uur (720 min).");
				RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
				rd.forward(req, resp);
				return;
			}
		}
		
		if (locatie_req.length() > 15) {
			req.setAttribute("errorLocatie","Dit veld mag maximaal 15 karakters bevatten.");
			RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
			rd.forward(req, resp);
			return;
		}
		
		Slb slb = (Slb) req.getSession().getAttribute("user");
		try {
			if (!sp.createAfspraak(datum, begintijd, eindtijd, checkbox_split_req, split, locatie_req, slb)) {
				req.setAttribute("errorBegintijd"," ");
				req.setAttribute("errorEindtijd","De geselecteerde tijden zijn al bezet! (Heeft u deze tijden al eerder opengesteld?)");
				RequestDispatcher rd =req.getRequestDispatcher("/uren_openstellen.jsp");            
				rd.forward(req, resp);
				return;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		resp.sendRedirect(req.getContextPath() + "/slb/"); 
	
	}
}