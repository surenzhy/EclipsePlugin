package org.eclipse.cq.ss.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javancss.Javancss;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.runtime.IPath;
import org.eclipse.cq.ss.beans.Function;
import org.eclipse.cq.ss.beans.Functions;
import org.eclipse.cq.ss.beans.JavancssResultBean;
import org.eclipse.cq.ss.ui.SortableTableModel;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class CodeComplexityMeasurementAction implements
		IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public CodeComplexityMeasurementAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				String outFilePath = runCCNStats(window);

				if (outFilePath != null) {
					File f = new File(outFilePath);
					if (null != f && f.exists()) {
						JavancssResultBean resultBean = generateCCNReport(outFilePath);

						createTableViewForCCNResult(resultBean);

						f.delete();
					}
				} else {
					MessageDialog
							.openInformation(window.getShell(),
									"Not Java Files Opened",
									"Please open a java file to measure code complexity!!");
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createTableViewForCCNResult(JavancssResultBean resultBean) {
		String[] columnNames = { "No.", "Method Name", "CCN", "NCSS", "Javadoc" };

		final JTable table = new JTable(getTableModel(resultBean, columnNames));
		table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
				.setHorizontalAlignment(JLabel.LEFT);
		resizeColumnWidth(table);
		table.setAutoCreateRowSorter(true);

		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		String modes[] = { "Resize All Columns", "Resize Last Column",
				"Resize Next Column", "Resize Off", "Resize Subsequent Columns" };

		final int modeKey[] = { JTable.AUTO_RESIZE_ALL_COLUMNS,
				JTable.AUTO_RESIZE_LAST_COLUMN, JTable.AUTO_RESIZE_NEXT_COLUMN,
				JTable.AUTO_RESIZE_OFF, JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS };

		JComboBox resizeModeComboBox = new JComboBox(modes);

		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				int index = source.getSelectedIndex();
				table.setAutoResizeMode(modeKey[index]);
			}
		};
		resizeModeComboBox.addItemListener(itemListener);

		JFrame frame = new JFrame("Code Complexity");
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(resizeModeComboBox, BorderLayout.NORTH);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
	}

	private JavancssResultBean generateCCNReport(String outFilePath)
			throws JAXBException, IOException {
		InputStream ins = new FileInputStream(new File(outFilePath));
		JAXBContext jaxbContext = JAXBContext
				.newInstance(JavancssResultBean.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		JavancssResultBean resultBean = (JavancssResultBean) jaxbUnmarshaller
				.unmarshal(ins);
		ins.close();
		return resultBean;
	}

	private String runCCNStats(IWorkbenchWindow window) throws IOException {
		IWorkbenchPage activePage = window == null ? null : window
				.getActivePage();

		IEditorPart editor = activePage == null ? null : activePage
				.getActiveEditor();
		IEditorInput input = editor == null ? null : editor.getEditorInput();
		IPath path = input instanceof FileEditorInput ? ((FileEditorInput) input)
				.getPath() : null;

		String outFilePath = null;
		if (null != path) {
			outFilePath = path.toOSString() + ".xml";
			String[] asArgs = new String[] { "-ncss", "-xml", "-function",
					"-out", outFilePath, "-recursive", path.toString() };
			Javancss pJavancss = new Javancss(asArgs);
		}
		return outFilePath;
	}

	public TableModel getTableModel(JavancssResultBean resultBean,
			String[] columnNames) {
		SortableTableModel dm = new SortableTableModel() {
			public Class getColumnClass(int col) {
				switch (col) {
				case 0:
					return Integer.class;
				case 1:
					return String.class;
				case 2:
					return Integer.class;
				case 3:
					return Integer.class;
				case 4:
					return Integer.class;
				default:
					return Object.class;
				}
			}

			public boolean isCellEditable(int row, int col) {

				return false;
			}

			public void setValueAt(Object obj, int row, int col) {

			}
		};
		dm.setDataVector(getTableData(resultBean), columnNames);
		return dm;
	}

	private void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 50; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width, width);
			}
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	private Object[][] getTableData(JavancssResultBean resultBean) {
		Object[][] data = null;
		if (null != resultBean && resultBean.getFunctions() != null) {
			Functions allFunctions = resultBean.getFunctions();
			List<Function> allFunctionsList = allFunctions.getFunction();
			int i = 0;
			data = new Object[allFunctionsList.size()][5];
			for (Iterator iterator = allFunctionsList.iterator(); iterator
					.hasNext();) {
				Function function = (Function) iterator.next();
				data[i][0] = i + 1;
				data[i][1] = getFunctionName(function.getName());
				data[i][2] = Integer.parseInt(function.getCcn());
				data[i][3] = Integer.parseInt(function.getNcss());
				data[i][4] = Integer.parseInt(function.getJavadocs());
				i++;
			}
		}

		return data;
	}

	private String getFunctionName(String name) {
		if(null != name){
			return name.substring(name.lastIndexOf(".") + 1, name.length());
		}
		return "";
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}