package errorcheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import AST.ASTNode;

public class VisitNodes {
	public boolean searchDown(ASTNode startPoint, Object targetForInvocation, String methodName, boolean singleMatch) {
		return searchDown(startPoint, targetForInvocation, methodName, singleMatch, null);
	}

	public boolean searchDown(ASTNode startPoint, Object targetForInvocation, String methodName, boolean singleMatch, Object extraArgument) {
        boolean match = false;
        Class methodParamClass = startPoint.getClass();
        while (methodParamClass != null) {
            try {
            	Method m;
            	if (extraArgument==null) {
                    m = targetForInvocation.getClass().getMethod(methodName, methodParamClass);            		
                	m.invoke(targetForInvocation.getClass().cast(targetForInvocation), startPoint);
            	}
            	else {
            		m = targetForInvocation.getClass().getMethod(methodName, methodParamClass, extraArgument.getClass());
                	m.invoke(targetForInvocation.getClass().cast(targetForInvocation), startPoint, extraArgument);
            	}

                //System.out.println( "searchDown for : '" + methodName + "' of "  + this.getClass() + " => OK");
                match = true;
                break;
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
            } catch (IllegalArgumentException e) {
            	e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                System.err.println("Methodname = " + methodName + " object " + startPoint);
            	e.printStackTrace();
            }
            methodParamClass = methodParamClass.getSuperclass();
        }
        if (match && singleMatch) return true;

        for (int i=0; i<startPoint.getNumChildNoTransform(); i++) {
            match = searchDown(startPoint.getChildNoTransform(i), targetForInvocation, methodName, singleMatch, extraArgument);
            if (match && singleMatch) return true;
        }

        return false;
    }

}
