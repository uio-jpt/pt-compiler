package errorcheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import AST.ASTNode;

public class VisitNodes {
	public boolean searchDown(ASTNode startPoint, Object targetForInvocation, String methodName, boolean singleMatch) {
        boolean match = false;
        Class methodParamClass = startPoint.getClass();
        while (methodParamClass != null) {
            try {
                Method m = targetForInvocation.getClass().getMethod(methodName, methodParamClass);
                m.invoke(targetForInvocation.getClass().cast(targetForInvocation), startPoint);
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
                e.printStackTrace();
            }
            methodParamClass = methodParamClass.getSuperclass();
        }
        if (match && singleMatch) return true;

        for (int i=0; i<startPoint.getNumChildNoTransform(); i++) {
            match = searchDown(startPoint.getChildNoTransform(i), targetForInvocation, methodName, singleMatch);
            if (match && singleMatch) return true;
        }

        return false;
    }

}
