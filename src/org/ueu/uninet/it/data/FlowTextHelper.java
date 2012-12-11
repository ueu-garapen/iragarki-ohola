package org.ueu.uninet.it.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.SpannableString;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;

public class FlowTextHelper {

       private static boolean mNewClassAvailable;

       /* class initialization fails when this throws an exception */


       static {
           try {
               Class.forName("android.text.style.LeadingMarginSpan$LeadingMarginSpan2");
               mNewClassAvailable = true;
           } catch (Exception ex) {
               mNewClassAvailable = false;
           }
       }

       public void tryFlowText(String text, View thumbnailView, TextView messageView, Display display){
           // There is nothing I can do for older versions, so just return
           if(!mNewClassAvailable) return;

           // Get height and width of the image and height of the text line
            thumbnailView.measure(display.getWidth(), display.getHeight());
            int height = thumbnailView.getMeasuredHeight();
            int width = thumbnailView.getMeasuredWidth();
            float textLineHeight = messageView.getPaint().getTextSize();

            // Set the span according to the number of lines and width of the image
            int lines = (int)Math.round(height / textLineHeight);
            SpannableString ss = new SpannableString(text);
            //For an html text you can use this line: SpannableStringBuilder ss = (SpannableStringBuilder)Html.fromHtml(text);
            MyLeadingMarginSpan2 MyLeadingMarginSpan2 = new MyLeadingMarginSpan2(lines, width+5);
            ss.setSpan(MyLeadingMarginSpan2, 0, ss.length(), 0);
            messageView.setText(ss);

            // Align the text with the image by removing the rule that the text is to the right of the image
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)messageView.getLayoutParams();
            int[]rules = params.getRules();
            rules[RelativeLayout.RIGHT_OF] = 0;
       }
       
       private class MyLeadingMarginSpan2 implements LeadingMarginSpan2 {
    	    private int margin;
    	    private int lines;

    	    MyLeadingMarginSpan2(int lines, int margin) {
    	        this.margin = margin;
    	        this.lines = lines;
    	    }

    	    /* Возвращает значение, на которе должен быть добавлен отступ */
    	    @Override
    	    public int getLeadingMargin(boolean first) {
    	        if (first) {
    	            /*
    	             * Данный отступ будет применен к количеству строк
    	             * возвращаемых getLeadingMarginLineCount()
    	             */
    	            return margin;
    	        } else {
    	            // Отступ для всех остальных строк
    	            return 0;
    	        }
    	    }

    	    @Override
    	    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, 
    	            int top, int baseline, int bottom, CharSequence text, 
    	            int start, int end, boolean first, Layout layout) {}

    	    /*
    	     * Возвращает количество строк, к которым должен быть применен
    	     * отступ возвращаемый методом getLeadingMargin(true)
    	     * Замечание. Отступ применяется только к N строкам одного параграфа.
    	     */
    	    @Override
    	    public int getLeadingMarginLineCount() {
    	        return lines;
    	    }
    	};
}

