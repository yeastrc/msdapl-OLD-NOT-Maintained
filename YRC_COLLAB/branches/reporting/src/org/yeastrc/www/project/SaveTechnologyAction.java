/*
 *
 * Created on February 5, 2004
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.grant.Grant;
import org.yeastrc.grant.ProjectGrantRecord;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.Technology;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * Controller class for saving a project.
 */
public class SaveTechnologyAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {
		int projectID;			// Get the projectID they're after
		
		// The form elements we're after
		String title = null;
		int pi = 0;
		int researcherB = 0;
		int researcherC = 0;
		int researcherD = 0;
		String[] groups = null;
		String projectAbstract = null;
		String publicAbstract = null;
		String progress = null;
		//String keywords = null;
		String publications = null;
		String comments;
		float bta = (float)0.0;
		String axisI = null;
		String axisII = null;
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}


		try {
			String strID = request.getParameter("ID");

			if (strID == null || strID.equals("")) {
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noprojectid"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}

			projectID = Integer.parseInt(strID);

		} catch (NumberFormatException nfe) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.invalidprojectid"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}

		// Load our project
		Technology project;
		
		try {
			project = (Technology)(ProjectFactory.getProject(projectID));
			if (!project.checkAccess(user.getResearcher())) {
				
				// This user doesn't have access to this project.
				ActionErrors errors = new ActionErrors();
				errors.add("username", new ActionMessage("error.project.noaccess"));
				saveErrors( request, errors );
				return mapping.findForward("Failure");
			}
		} catch (Exception e) {
			
			// Couldn't load the project.
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.project.projectnotfound"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");	
		}

		// Set this project in the request, as a bean to be displayed on the view
		//request.setAttribute("project", project);

		// We're saving!
		title = ((EditTechnologyForm)(form)).getTitle();
		pi = ((EditTechnologyForm)(form)).getPI();
		researcherB = ((EditTechnologyForm)(form)).getResearcherB();
		researcherC = ((EditTechnologyForm)(form)).getResearcherC();
		researcherD = ((EditTechnologyForm)(form)).getResearcherD();
		groups = ((EditTechnologyForm)(form)).getGroups();
		projectAbstract = ((EditTechnologyForm)(form)).getAbstract();
		publicAbstract = ((EditTechnologyForm)(form)).getPublicAbstract();
		//keywords = ((EditTechnologyForm)(form)).getKeywords();
		progress = ((EditTechnologyForm)(form)).getProgress();
		publications = ((EditTechnologyForm)(form)).getPublications();
		comments = ((EditTechnologyForm)(form)).getComments();
		bta = ((EditTechnologyForm)(form)).getBTA();
		axisI = ((EditTechnologyForm)(form)).getAxisI();
		axisII = ((EditTechnologyForm)(form)).getAxisII();
		
		// Set blank items to null
		if (title.equals("")) title = null;
		if (projectAbstract.equals("")) projectAbstract = null;
		//if (keywords.equals("")) keywords = null;
		if (progress.equals("")) progress = null;
		if (publications.equals("")) publications = null;
		if (comments.equals("")) comments = null;
		if (axisI != null && axisI.equals("")) axisI = null;
		if (axisII != null && axisII.equals("")) axisII = null;
		
		// Set up our researchers
		Researcher oPI = null;
		Researcher orB = null;
		Researcher orC = null;
		Researcher orD = null;		
		try {
			if (pi != 0) {
				oPI = new Researcher();
				oPI.load(pi);
			}			
			if (researcherB != 0) {
				orB = new Researcher();
				orB.load(researcherB);
			}
			
			if (researcherC != 0) {
				orC = new Researcher();
				orC.load(researcherC);
			}
			
			if (researcherD != 0) {
				orD = new Researcher();
				orD.load(researcherD);
			}
		} catch (InvalidIDException iie) {

			// Couldn't load the researcher.
			ActionErrors errors = new ActionErrors();
			errors.add("project", new ActionMessage("error.project.invalidresearcher"));
			saveErrors( request, errors );
			return mapping.findForward("Failure");
		}


		// Set up the groups
		project.clearGroups();
		
		if (groups != null) {
			if (groups.length > 0) {
				for (int i = 0; i < groups.length; i++) {
					try { project.setGroup(groups[i]); }
					catch (InvalidIDException iie) {
					
						// Somehow got an invalid group...
						ActionErrors errors = new ActionErrors();
						errors.add("project", new ActionMessage("error.project.invalidgroup"));
						saveErrors( request, errors );
						return mapping.findForward("Failure");					
					}
				}
			}
		}

		// Set all of the new values in the project
		project.setTitle(title);
		project.setPI(oPI);
		project.setResearcherB(orB);
		project.setResearcherC(orC);
		project.setResearcherD(orD);
		project.setAbstract(projectAbstract);
		project.setPublicAbstract(publicAbstract);
		//project.setKeywords(keywords);
		project.setProgress(progress);
		project.setPublications(publications);
		project.setComments(comments);
		project.setBTA(bta);
		
		// Save the project
		project.save();

		// save the project grants
		List<Grant> grants = ((EditTechnologyForm)(form)).getGrantList();
		ProjectGrantRecord.getInstance().saveProjectGrants(project.getID(), grants);
		
		// Go!
		return mapping.findForward("viewProject");

	}
	
}