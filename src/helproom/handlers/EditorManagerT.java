package helproom.handlers;

import helproom.ChatT;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class EditorManagerT extends AbstractHandler {

	String s = "teacher";
	String id = "-1";
	Firebase fb;
	Map<String, Map<String, String>> bm;
	MyDialog d;
	IDocument doc;
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		fb = new Firebase("https://boiling-fire-5079.firebaseio.com/");
		getCurrentEditorContent();
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		d = new MyDialog(window.getShell(), "Help Someone",
				"Input the ID number of the student", null, new IInputValidator() {
					public String isValid(String newText){
						try{
							Integer.parseInt(newText);
						}
						catch(NumberFormatException e){
							return "Input an ID number.";
						}
						if(!bm.containsKey(newText))
							return "ID Number not found.";
						return null;
					}
				});
		d.open();
		
		
		return null;
	}

	public String getCurrentEditorContent() {
		final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(editor instanceof ITextEditor))
			return null;
		ITextEditor ite = (ITextEditor) editor;
		doc = ite.getDocumentProvider().getDocument(
				ite.getEditorInput());
		// fb.setValue(doc.get());
		
		
		
		fb.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snap) {
				bm = (Map<String, Map<String, String>>) snap.getValue();
				Map<String, String> m = bm.get(id);
				if (m != null) {
					if(m.containsKey("full")){
						doc.set(m.get("full"));
						bm.get(id).remove("full");
					}
					s = m.get("last");
					if (s.equals("student")) {

						//System.out.println(m.get("text"));
						try {
							doc.replace(Integer.parseInt(m.get("offset")),
									Integer.parseInt(m.get("length")),
									m.get("text"));
						} catch (Exception e) {
							System.out.println("Bad");

						}
					}
				}

			}

			@Override
			public void onCancelled(FirebaseError error) {
			}
		});
		return doc.get();
	}
	
	class MyDialog extends InputDialog{

		Shell shelly;
		public MyDialog(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			shelly = parentShell;
		}
		
		protected void okPressed(){
			String sid = getText().getText();
			if(bm.containsKey(sid)){
				close();
				id = sid;
				doc.addDocumentListener(new IDocumentListener() {

					@Override
					public void documentChanged(DocumentEvent event) {

						// System.out.println("Change happened: " + event.toString());
					}

					@Override
					public void documentAboutToBeChanged(DocumentEvent event) {
						// System.out.println("I predict that the following change will occur: "+
						// event.toString());
						if (s.equals("teacher")) {
							HashMap<String, String> m = new HashMap<String, String>();
							m.put("length", Integer.toString(event.getLength()));
							m.put("offset", Integer.toString(event.getOffset()));
							m.put("ModificationStamp",
									Long.toString(event.getModificationStamp()));
							m.put("text", event.getText());
							m.put("last", "teacher");
							//m.put("full", doc.get());
							bm.put(id, m);
							fb.setValue(bm);
						}
						s = "teacher";
					}
				});
				MessageDialog.openInformation(shelly, "Connected", "You connected to " + sid + ".");
				
				try {
					ChatT windowc = new ChatT();
					windowc.open(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			else 
				MessageDialog.openInformation(shelly, "Failed", "ID number not found.");
		}
		
	}

}
