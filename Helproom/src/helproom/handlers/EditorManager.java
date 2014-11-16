package helproom.handlers;

import helproom.Chat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class EditorManager extends AbstractHandler {

	String s = "student";
	Firebase fb;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("here");
		fb = new Firebase("https://boiling-fire-5079.firebaseio.com/");

		System.out.println("there");

		System.out.println(getCurrentEditorContent());
		try {
			Chat window = new Chat();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getCurrentEditorContent() {
		final IEditorPart editor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(editor instanceof ITextEditor))
			return null;
		ITextEditor ite = (ITextEditor) editor;
		IDocument doc = ite.getDocumentProvider().getDocument(
				ite.getEditorInput());
		// fb.setValue(doc.get());
		doc.addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {

				// System.out.println("Change happened: " + event.toString());
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// System.out.println("I predict that the following change will occur: "+
				// event.toString());
				if (s.equals("student")) {
					HashMap<String, String> m = new HashMap<String, String>();
					m.put("length", Integer.toString(event.getLength()));
					m.put("offset", Integer.toString(event.getOffset()));
					m.put("ModificationStamp",
							Long.toString(event.getModificationStamp()));
					m.put("text", event.getText());
					m.put("last", "student");
					//m.put("full", doc.get());
					fb.setValue(m);
				}
				s = "student";
			}
		});
		
		
		fb.addValueEventListener(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snap) {
				
				Map<String, String> m = (Map<String, String>) snap.getValue();
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

			@Override
			public void onCancelled(FirebaseError error) {
			}
		});
		return doc.get();
	}

}
