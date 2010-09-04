package errorcheck;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import AST.ClassDecl;
import AST.CompilationUnit;
import AST.PTDecl;
import AST.PTDummyClass;
import AST.PTInstDecl;
import AST.PTPackage;
import AST.SimpleClass;
import AST.TemplateMethodAccess;

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
        
//        v = new VisitNodes() {
//        	List<String> constructorNames = Lists.newLinkedList();
//        	Set<String> visited = Sets.newHashSet();
//        	        	
//        	public void find(PTPackage p) {
//        		searchDown(p, this, "handleClass", false);
//        	}
//        	
//        	public void recurseInstantiations(PTDummyClass p, String fromClass) {
//        		//System.out.println("Looking at " + p + " with " + fromClass);
//        		if (p.getID().equals(fromClass)) {
//        			PTInstDecl inst = (PTInstDecl)p.getParentClass(PTInstDecl.class);
//        		
//        			//getOriginator(p.getParentClass(CompilationUnit.class), inst.getID(), p.getOrgID());
//        			
//        			//SimpleClass s = (SimpleClass)p.getOriginator().getParentClass(SimpleClass.class);
//        			//if (s!=null) handleClass(s);
//        		}
//        	}
//        	
//        	public void handleClass(SimpleClass p) {
//            	constructorNames = Lists.newLinkedList();
//            	visited = Sets.newHashSet();
//            	
//            	System.out.println("Looking at class " + p.getID());
//            	String topSuperClassName = p.getTopMostSuperName();
//            	String key = p.getPTDecl().getID() + "::" + "hmm";
//            	
//            	if (!visited.contains(key)) {
//            		visited.add(key);
//            		constructorNames.add(p.getPTDecl().getID() + "::" + p.getClassDecl().getID());
//            	}
//            	
//            	PTDecl scope = (PTDecl) p.getParentClass(PTDecl.class);
//            	searchDown(scope, this, "recurseInstantiations", false, p.getID());
//        	}
//        };
        
        // I hver simpleclass node:
        
        /* key = getTemplate().getID() + getTopSuperName()
         * if key not in visited:
         *    visited add key
         *    list add (tmplate name + thisClassID())
         * expand instantiations
         * move up to super
         */
        //v.searchDown(unit, v, "find", false);
        
        final HashMap<String, ClassDecl> globalScope = new HashMap<String, ClassDecl>();
        //final HashMap<String, String> instantiations = new HashMap<String, String>();
        final HashMap<String, String> instantiationsReverse = new HashMap<String, String>();
        
        VisitNodes classHierarchy = new VisitNodes() {
        	public void find(ClassDecl cd) {
        		PTDecl scope = (PTDecl)cd.getParentClass(PTDecl.class);
        		String key = scope.getID() + "::" + cd.getID();
        		globalScope.put(key, cd);
        		System.out.println("class " + key + " extends " + cd.getSuperClassName());
        	}
        	
        	public void find(PTDummyClass instTuple) {
        		PTDecl scope = (PTDecl)instTuple.getParentClass(PTDecl.class);
        		PTInstDecl importedFromScope = (PTInstDecl)instTuple.getParentClass(PTInstDecl.class);
        		String fromKey = importedFromScope.getID() + "::" + instTuple.getOrgID();
        		String toKey = scope.getID() + "::" + instTuple.getID();
        		System.out.println("inst " + fromKey + " => " + toKey);
        		//instantiations.put(fromKey, toKey);
        		instantiationsReverse.put(toKey, fromKey);
        	}
        };
        
        classHierarchy.searchDown(unit, classHierarchy, "find", false);
        
	}
}
