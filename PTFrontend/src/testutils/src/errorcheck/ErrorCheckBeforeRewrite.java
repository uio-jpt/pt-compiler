package errorcheck;

import java.util.List;
import java.util.Set;

import AST.ClassDecl;
import AST.CompilationUnit;
import AST.PTDecl;
import AST.PTInstDecl;
import AST.PTPackage;
import AST.SimpleClass;
import AST.TemplateMethodAccess;
import AST.TypeAccess;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ErrorCheckBeforeRewrite {

	public void checkErrors(CompilationUnit unit) {
        VisitNodes v = new VisitNodes() {
        	private String templateID;
        	private boolean ok;
        	
        	public void checkTemplate(PTInstDecl p) {
        		if (p.getID().equals(templateID)) {
        			ok = true;
        		}
        	}
        	
        	public void find(TemplateMethodAccess p) {
        		System.out.println("checking " + p);
        		ok = false;
        		templateID = p.getTemplateID();
        		PTDecl scope = (PTDecl)p.getParentClass(PTDecl.class);
        		searchDown(scope, this, "checkTemplate", false);
        		if (ok==false) {
        			p.error("Template " + p.getTemplateID() + " is not visible in current scope.");
        		}
        	}
        };
        v.searchDown(unit, v, "find", false);
        
        v = new VisitNodes() {
        	List<String> constructorNames = Lists.newLinkedList();
        	Set<String> visited = Sets.newHashSet();
        	        	
        	public void find(PTPackage p) {
        		searchDown(p, this, "handleClass", false);
        	}
        	
        	public void recurseInstantiations(PTDummyClass p) {
        	}
        	
        	public void handleClass(SimpleClass p) {
            	constructorNames = Lists.newLinkedList();
            	visited = Sets.newHashSet();
            	
            	String topSuperClassName = p.getTopSupername();
            	String key = p.getPTDecl().getID() + "::" + topSuperClassName;
            	
            	if (!visited.contains(key)) {
            		visited.add(key);
            		constructorNames.add(p.getPTDecl().getID() + "::" + p.getClassDecl().getID());
            	}
            	
            	PTDecl scope = (PTDecl) p.getParentClass(PTDecl.class);
            	searchDown(scope, this, "recurseInstantiations", false);
            	
        	}
        };
        
        // I hver simpleclass node:
        
        /* key = getTemplate().getID() + getTopSuperName()
         * if key not in visited:
         *    visited add key
         *    list add (tmplate name + thisClassID())
         * expand instantiations
         * move up to super
         */
        
        v.searchDown(unit, v, "find", false);
	}
}
