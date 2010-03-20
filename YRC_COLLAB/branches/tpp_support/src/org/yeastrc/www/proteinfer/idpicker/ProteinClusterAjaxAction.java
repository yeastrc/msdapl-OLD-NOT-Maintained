package org.yeastrc.www.proteinfer.idpicker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.yeastrc.ms.domain.protinfer.PeptideDefinition;
import org.yeastrc.ms.domain.protinfer.ProteinFilterCriteria;
import org.yeastrc.www.proteinfer.ProteinInferSessionManager;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

public class ProteinClusterAjaxAction extends Action{

    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {


        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            response.getWriter().write("You are not logged in!");
            response.setStatus(HttpServletResponse.SC_SEE_OTHER); // Status code (303) indicating that the response to the request can be found under a different URI.
            return null;
        }

        int pinferId = 0;
        try {pinferId = Integer.parseInt(request.getParameter("pinferId"));}
        catch(NumberFormatException e) {}

        if(pinferId == 0) {
            response.setContentType("text/html");
            response.getWriter().write("<b>Invalid Protein Inference ID: "+pinferId+"</b>");
            return null;
        }
        

        int clusterId = 0;
        try {clusterId = Integer.parseInt(request.getParameter("clusterId"));}
        catch(NumberFormatException e) {}

        if(clusterId == 0) {
            response.setContentType("text/html");
            response.getWriter().write("<b>Invalid Protein Cluster ID: "+clusterId+"</b>");
            return null;
        }

        // Peptide definition from the session
        ProteinFilterCriteria filterCriteria = (ProteinFilterCriteria) request.getSession().getAttribute("pinferFilterCriteria");
        if(filterCriteria == null)  filterCriteria = new ProteinFilterCriteria();
        PeptideDefinition peptideDef = filterCriteria.getPeptideDefinition();
        
        System.out.println("Got request for clusterId: "+clusterId+" of protein inference run: "+pinferId);

        request.setAttribute("pinferId", pinferId);
        request.setAttribute("clusterId", clusterId);
        
        
        WIdPickerCluster cluster = IdPickerResultsLoader.getIdPickerCluster(pinferId, clusterId, peptideDef);
        request.setAttribute("cluster", cluster);
        
        return mapping.findForward("Success");
    }
   
}
