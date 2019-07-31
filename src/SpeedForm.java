import javax.swing.*;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;

/*
 * Created by JFormDesigner on Tue Jul 30 01:11:43 BRT 2019
 */
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JLabel;


/*
 * Created by JFormDesigner on Tue Jul 30 01:11:43 BRT 2019
 */

import javax.swing.JProgressBar;


/**
 * @author User #4
 */
public class SpeedForm extends JDialog
{
    public SpeedForm()
    {
        initComponents();
        name1.setText( "WAN1" );
        name2.setText( "WAN2" );
        name3.setText( "WAN3" );
        status1.setOpaque( true );
        status2.setOpaque( true );
        status3.setOpaque( true );
        setAlwaysOnTop( true );
        setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
        //setOpacity( 0.70F );
    }

    public void updateControls( final long waitTimeMs )
    {
        if( SnmpClientTestKt.getLinkWan1() != null ) {
            status1.setBackground( SnmpClientTestKt.getLinkWan1() == 0 ? Color.RED : Color.GREEN );
            status1.setText( SnmpClientTestKt.getLinkWan1() == 0 ? "OFF" : "ON" );
        }
        if( SnmpClientTestKt.getLinkWan2() != null ) {
            status2.setBackground( SnmpClientTestKt.getLinkWan2() == 0 ? Color.RED : Color.GREEN );
            status2.setText( SnmpClientTestKt.getLinkWan2() == 0 ? "OFF" : "ON" );
        }
        if( SnmpClientTestKt.getLinkWan3() != null ) {
            status3.setBackground( SnmpClientTestKt.getLinkWan3() == 0 ? Color.RED : Color.GREEN );
            status3.setText( SnmpClientTestKt.getLinkWan3() == 0 ? "OFF" : "ON" );
        }
        final int megabyte = ( 1000 * 1000 ) / 8;
        if( SnmpClientTestKt.getWan1InDelta() != null && SnmpClientTestKt.getWan1OutDelta() != null ) {
            final Delta inDelta = calculateStatusBar( SnmpClientTestKt.getWan1InDelta(), waitTimeMs, ( 70 ) * megabyte );
            final Delta outDelta = calculateStatusBar( SnmpClientTestKt.getWan1OutDelta(), waitTimeMs, ( 10 ) * megabyte );
            progressBar1in.setValue( ( int )( inDelta.percentage * 100 ) );
            delta1in.setText( formatDeltaText( inDelta.delta ) );
            progressBar1out.setValue( ( int )( outDelta.percentage * 100 ) );
            delta1out.setText( formatDeltaText( outDelta.delta ) );
        }
        if( SnmpClientTestKt.getWan2InDelta() != null && SnmpClientTestKt.getWan2OutDelta() != null ) {
            final Delta inDelta = calculateStatusBar( SnmpClientTestKt.getWan2InDelta(), waitTimeMs, ( 60 ) * megabyte );
            final Delta outDelta = calculateStatusBar( SnmpClientTestKt.getWan2OutDelta(), waitTimeMs, ( 60 ) * megabyte );
            progressBar2in.setValue( ( int )( inDelta.percentage * 100 ) );
            delta2in.setText( formatDeltaText( inDelta.delta ) );
            progressBar2out.setValue( ( int )( outDelta.percentage * 100 ) );
            delta2out.setText( formatDeltaText( outDelta.delta ) );
        }
        if( SnmpClientTestKt.getWan3InDelta() != null && SnmpClientTestKt.getWan3OutDelta() != null ) {
            final Delta inDelta = calculateStatusBar( SnmpClientTestKt.getWan3InDelta(), waitTimeMs, ( 60 ) * megabyte );
            final Delta outDelta = calculateStatusBar( SnmpClientTestKt.getWan3OutDelta(), waitTimeMs, ( 60 ) * megabyte );
            progressBar3in.setValue( ( int )( inDelta.percentage * 100 ) );
            delta3in.setText( formatDeltaText( inDelta.delta ) );
            progressBar3out.setValue( ( int )( outDelta.percentage * 100 ) );
            delta3out.setText( formatDeltaText( outDelta.delta ) );
        }
    }

    private String formatDeltaText( double delta )
    {
        final double mb = delta / ( 1000.0 * 1000.0 );
        return String.format( "%.2f MB/s", mb );
    }

    private Delta calculateStatusBar( final long delta, final long waitTimeMs, final long max )
    {
        final long divide = waitTimeMs / 1000;
        final double bySec = ( double )delta / ( double )divide;
        final double percentage = ( double )bySec / ( double )max;
        System.out.println( "total: " + delta + ", bySec: " + bySec + ", max: " + max + ", percentage: " + percentage );

        return new Delta( bySec, percentage );
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		name1 = new JLabel();
		status1 = new JLabel();
		delta1in = new JLabel();
		progressBar1in = new JProgressBar();
		delta1out = new JLabel();
		progressBar1out = new JProgressBar();
		name2 = new JLabel();
		status2 = new JLabel();
		delta2in = new JLabel();
		progressBar2in = new JProgressBar();
		delta2out = new JLabel();
		progressBar2out = new JProgressBar();
		name3 = new JLabel();
		status3 = new JLabel();
		delta3in = new JLabel();
		progressBar3in = new JProgressBar();
		delta3out = new JLabel();
		progressBar3out = new JProgressBar();

		//======== this ========
		setTitle("WAN LINKS");
		Container contentPane = getContentPane();
		contentPane.setLayout(new FormLayout(
			"3*(default, $lcgap), default:grow, $lcgap, default, $lcgap, default:grow",
			"2*(default, $lgap), default"));

		//---- name1 ----
		name1.setText("text");
		contentPane.add(name1, CC.xy(1, 1));

		//---- status1 ----
		status1.setText("text");
		contentPane.add(status1, CC.xy(3, 1));
		contentPane.add(delta1in, CC.xy(5, 1));
		contentPane.add(progressBar1in, CC.xy(7, 1));
		contentPane.add(delta1out, CC.xy(9, 1));
		contentPane.add(progressBar1out, CC.xy(11, 1));

		//---- name2 ----
		name2.setText("text");
		contentPane.add(name2, CC.xy(1, 3));

		//---- status2 ----
		status2.setText("text");
		contentPane.add(status2, CC.xy(3, 3));
		contentPane.add(delta2in, CC.xy(5, 3));
		contentPane.add(progressBar2in, CC.xy(7, 3));
		contentPane.add(delta2out, CC.xy(9, 3));
		contentPane.add(progressBar2out, CC.xy(11, 3));

		//---- name3 ----
		name3.setText("text");
		contentPane.add(name3, CC.xy(1, 5));

		//---- status3 ----
		status3.setText("text");
		contentPane.add(status3, CC.xy(3, 5));
		contentPane.add(delta3in, CC.xy(5, 5));
		contentPane.add(progressBar3in, CC.xy(7, 5));
		contentPane.add(delta3out, CC.xy(9, 5));
		contentPane.add(progressBar3out, CC.xy(11, 5));
		pack();
		setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel name1;
	private JLabel status1;
	private JLabel delta1in;
	private JProgressBar progressBar1in;
	private JLabel delta1out;
	private JProgressBar progressBar1out;
	private JLabel name2;
	private JLabel status2;
	private JLabel delta2in;
	private JProgressBar progressBar2in;
	private JLabel delta2out;
	private JProgressBar progressBar2out;
	private JLabel name3;
	private JLabel status3;
	private JLabel delta3in;
	private JProgressBar progressBar3in;
	private JLabel delta3out;
	private JProgressBar progressBar3out;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    class Delta
    {
        final double delta;
        final double percentage;

        public Delta( double delta, double percentage )
        {
            this.delta = delta;
            this.percentage = percentage;
        }
    }
}
