package ch.idsia.ai.agents.human;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;



public class MouseMotionListenerAgent implements MouseMotionListener{

	 public Point goal = new Point(0,0);
	
	 void eventOutput(String eventDescription, MouseEvent e) {
		 PointerInfo a = MouseInfo.getPointerInfo();
			Point point = new Point(a.getLocation());
			SwingUtilities.convertPointToScreen(point, e.getComponent());
			int x=(int) point.getX();
			int y=(int) point.getY();
//			System.out.print("Y: "+y);
//			System.out.print("X: "+x+"\n");
			this.goal = new Point(x,y);
	    }
	     
	    public void mouseMoved(MouseEvent e) {
	        eventOutput("Mouse moved", e);
	    }
	     
	    public void mouseDragged(MouseEvent e) {
	        eventOutput("Mouse dragged", e);
	    }                

}
