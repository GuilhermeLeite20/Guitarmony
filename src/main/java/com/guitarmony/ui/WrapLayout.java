package com.guitarmony.ui;

import java.awt.*;

/** Flow layout whose preferred height includes wrapped rows on small windows. */
final class WrapLayout extends FlowLayout {
    WrapLayout(int gap) { super(FlowLayout.LEFT,gap,0); }
    @Override public Dimension preferredLayoutSize(Container target) {
        synchronized(target.getTreeLock()) {
            int width=target.getWidth();
            Container parent=target.getParent();
            while(width<=0&&parent!=null){width=parent.getWidth();parent=parent.getParent();}
            if(width<=0)width=1450;
            Insets insets=target.getInsets();
            int available=Math.max(1,width-insets.left-insets.right-getHgap()*2);
            int used=0,rowHeight=0,totalHeight=0,maxWidth=0;
            for(Component child:target.getComponents())if(child.isVisible()) {
                Dimension d=child.getPreferredSize();int gap=used==0?0:getHgap();
                if(used>0&&used+gap+d.width>available){
                    maxWidth=Math.max(maxWidth,used);totalHeight+=rowHeight+8;used=0;rowHeight=0;gap=0;
                }
                used+=gap+d.width;rowHeight=Math.max(rowHeight,d.height);
            }
            maxWidth=Math.max(maxWidth,used);totalHeight+=rowHeight;
            return new Dimension(maxWidth+insets.left+insets.right+getHgap()*2,
                    totalHeight+insets.top+insets.bottom+getVgap()*2);
        }
    }
    @Override public Dimension minimumLayoutSize(Container target) {
        Dimension preferred=preferredLayoutSize(target);return new Dimension(0,preferred.height);
    }
}
