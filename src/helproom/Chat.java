package helproom;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import javax.*;
import javax.sound.*;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class Chat {

	protected Shell shlStudentTeacherChat;
	private static Text text;
	private static Text text_1;
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	static Firebase fb;
	static Map<String, Map<String, String>> bm;
	String id;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Chat window = new Chat();
			window.open("100");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void addText(String s) {
		text.append("/n" + s);

	}

	static class Mouse implements MouseListener {

		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			String temp1 = Chat.text.getText();
			String temp2 = Chat.text_1.getText();
			Chat.text.append("\nme: " + temp2);
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("text", temp2);
			m.put("last", "student");
			fb.setValue(m);
		}

		public void mouseUp(MouseEvent e) {
			String temp1 = Chat.text.getText();
			String temp2 = Chat.text_1.getText();
			Chat.text_1.setText("");
		}
	}

	/**
	 * Open the window.
	 */
	public void open(String sid) {

		id = sid;
		Display display = Display.getDefault();
		// System.out.println(display.getThread() == Thread.currentThread());
		createContents();
		shlStudentTeacherChat.open();
		shlStudentTeacherChat.layout();
		fb = new Firebase("https://sweltering-fire-8198.firebaseio.com/");
		fb.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snap) {
				bm = (Map<String, Map<String, String>>) snap.getValue();
				if (bm == null)
					bm = new HashMap<String, Map<String, String>>();
				if (bm.containsKey(id)) {
					if (!bm.get(id).get("last").equals("student")) {
						String s = bm.get(id).get("text");
						// System.out.println(s);
						display.syncExec(new Runnable() {
							public void run() {
								text.append("\nTeacher: " + s);
							}
						});
					}
				}
				// System.out.println("hello");
			}

			@Override
			public void onCancelled(FirebaseError error) {
			}
		});

		text_1.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.CR) {
					String temp1 = Chat.text.getText();
					String temp2 = Chat.text_1.getText();
					Chat.text.append("\nme: " + temp2);
					HashMap<String, String> m = new HashMap<String, String>();
					m.put("text", temp2);
					m.put("last", "student");
					bm.put(id, m);
					fb.setValue(bm);

				}
			}

			public void keyReleased(KeyEvent event) {
				if (event.keyCode == SWT.CR) {
					String temp1 = Chat.text.getText();
					String temp2 = Chat.text_1.getText();
					Chat.text_1.setText("");
				}

			}
		});

		while (!shlStudentTeacherChat.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlStudentTeacherChat = new Shell();
		shlStudentTeacherChat.setImage(SWTResourceManager
				.getImage("C:\\Users\\The Mamba\\Pictures\\apple.jpg"));
		shlStudentTeacherChat.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		shlStudentTeacherChat.setSize(450, 300);
		shlStudentTeacherChat.setText("Student Teacher Chat Client");

		text = new Text(shlStudentTeacherChat, SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL);
		text.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		text.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		text.setBounds(10, 40, 414, 90);

		text_1 = new Text(shlStudentTeacherChat, SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		text_1.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		text_1.setBounds(10, 157, 230, 94);

		Button btnSubmit = new Button(shlStudentTeacherChat, SWT.NONE);
		btnSubmit.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnSubmit.setBounds(280, 189, 75, 25);
		btnSubmit.setText("Submit");
		btnSubmit.addMouseListener(new Mouse());

		Label lblEnterTextBelow = formToolkit.createLabel(
				shlStudentTeacherChat, "Enter Text Below", SWT.NONE);
		lblEnterTextBelow.setFont(SWTResourceManager.getFont("Arial", 9,
				SWT.NORMAL));
		lblEnterTextBelow.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblEnterTextBelow.setBounds(10, 136, 93, 15);

		Label lblChat = formToolkit.createLabel(shlStudentTeacherChat, "Chat",
				SWT.NONE);
		lblChat.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblChat.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblChat.setBounds(10, 19, 31, 15);

	}
}
