package helproom.handlers;

import helproom.Chat;
import helproom.ChatT;
import helproom.handlers.EditorManagerT.MyDialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
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

public class EditorManager extends AbstractHandler {

	String id;
	String s = "student";
	Firebase fb;
	Map<String, Map<String, String>> bm;
	IDocument doc;
	MyDialog d;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		fb = new Firebase("https://boiling-fire-5079.firebaseio.com/");
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		getCurrentEditorContent();
		d = new MyDialog(window.getShell(), "Ask for Help",
				"Input your question.", null,null);
		d.open();
		return null;
	}

	public String getCurrentEditorContent() {
		final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(editor instanceof ITextEditor))
			return null;
		ITextEditor ite = (ITextEditor) editor;
		doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
		// fb.setValue(doc.get());

		fb.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snap) {
				bm = (Map<String, Map<String, String>>) snap.getValue();
				if (bm == null)
					bm = new HashMap<String, Map<String, String>>();
				Map<String, String> m = bm.get(id);
				if (m != null) {
					s = m.get("last");
					if (s.equals("teacher")) {

						System.out.println(m.get("text"));
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

	class MyDialog extends InputDialog {

		Shell shelly;

		public MyDialog(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue,
					validator);
			shelly = parentShell;
		}
		String question;
		protected void okPressed() {
			question = getText().getText();
			id = Integer.toString((int) (Math.random() * 10000));
			Map<String, String> map = new HashMap<String, String>();
			map.put("question", question);
			map.put("last", "student");
			map.put("full", doc.get());
			bm.put(id, map);
			fb.setValue(bm);
			doc.addDocumentListener(new IDocumentListener() {

				@Override
				public void documentChanged(DocumentEvent event) {
					
				}

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					System.out.println("hi");
					if (s.equals("student")) {
						HashMap<String, String> m = new HashMap<String, String>();
						m.put("length", Integer.toString(event.getLength()));
						m.put("offset", Integer.toString(event.getOffset()));
						m.put("ModificationStamp",
								Long.toString(event.getModificationStamp()));
						m.put("text", event.getText());
						m.put("last", "student");
						m.put("question", question);
						if(bm.get(id).containsKey("full")) m.put("full", doc.get());
						bm.put(id, m);
						fb.setValue(bm);
					}
					s = "student";
				}
			});
			close();
			MessageDialog.openInformation(shelly, "Question Posted",
					"Your question has been posted");

			try {
				Chat windowc = new Chat();
				windowc.open(id);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
