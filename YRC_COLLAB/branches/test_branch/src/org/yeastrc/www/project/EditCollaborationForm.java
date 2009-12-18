/*
 * RegisterForm.java
 *
 * Created on October 17, 2003
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Project;

/**
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version 2004-01-21
 */
public class EditCollaborationForm extends EditProjectForm {

	/**
	 * Validate the properties that have been sent from the HTTP request,
	 * and return an ActionErrors object that encapsulates any
	 * validation errors that have been found.  If no errors are found, return
	 * an empty ActionErrors object.
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = super.validate(mapping, request);
		
		// we need atleast one grant
		if (validGrantCount() < 1) {
		    // there could be grant information for the project entered via the old interface.
		    // if so, we will not return an error message
		    Project project = (Project) request.getSession().getAttribute("project");
		    if (project == null ||
		        project.getFundingTypes() == null ||
		        project.getFundingTypes().length() < 1) {
		        errors.add("grants", new ActionMessage("error.grant.nogrants"));
		    }
		}
		
//		if (this.getFundingTypes() == null || this.getFundingTypes().length < 1) {
//			errors.add("fundingTypes", new ActionMessage("error.project.nofundingtypes"));
//		} else {
//			
//			for (int i = 0; i < this.getFundingTypes().length; i++) {
//				if (this.getFundingTypes()[i].equals("FEDERAL")) {
//					if (this.getFederalFundingTypes() == null || this.getFederalFundingTypes().length < 1) {
//						errors.add("fundingTypes", new ActionMessage("error.project.nofederalfundingtypes"));
//						break;
//					}					
//				}
//				
//				if (!this.getFundingTypes()[i].equals("FEDERAL")) {
//					if (this.getFoundationName() == null || this.getFoundationName().length() < 2) {
//						errors.add("fundingTypes", new ActionMessage("error.project.nofoundationname"));
//						break;
//					}
//				}
//				
//			}
//		}
		
		if (this.getTitle() == null || this.getTitle().length() < 1) {
			errors.add("title", new ActionMessage("error.project.notitle"));
		}
		
		if (this.getAbstract() == null || this.getAbstract().length() < 1) {
			errors.add("project", new ActionMessage("error.project.noabstract"));
		}
		
		/*
		if (this.getKeywords() == null || this.getKeywords().length() < 1) {
			errors.add("project", new ActionMessage("error.project.nokeywords"));
		}
		*/
		
		String[] groups = this.getGroups();
		if (groups == null || groups.length < 1) {
			errors.add("groups", new ActionMessage("error.collaboration.nogroups"));
		}



		return errors;

	}


	/** Set the groups */
	public void setGroups(String[] groups) {
		if (groups != null) { this.groups = groups; }
	}

	/** Set whether or not to send emails */
	public void setSendEmail(boolean arg) {
		this.sendEmail = arg;
	}

	/** Set whether or not this will be saved as a tech dev project */
	public void setIsTech(boolean arg) { this.isTech = arg; }
	
	/** Get the groups */
	public String[] getGroups() { return this.groups; }

	/** Get whether or not to send email */
	public boolean getSendEmail() { return this.sendEmail; }
	
	/** Get whether or not this will be saved as a tech dev project */
	public boolean getIsTech() { return this.isTech; }

	/** The groups for this collaboration */
	private String[] groups = new String[0];
	
	/** Whether or not to send email to YRC groups */
	private boolean sendEmail = true;
	
	/** Whether or not this is a technology development project */
	private boolean isTech = false;

}