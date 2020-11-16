/****************************************************************************
 *
 *   Copyright (c) 2017,2018 Eike Mansfeld ecm@gmx.de. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 ****************************************************************************/

package com.comino.flight.ui.tabs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.comino.flight.FXMLLoadHelper;
import com.comino.flight.observables.StateProperties;
import com.comino.mavcom.control.IMAVController;
import com.comino.mavcom.mavlink.IMAVLinkListener;
import com.comino.mavutils.legacy.ExecutorService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableColumn.SortType;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class MAVInspectorTab extends Pane implements IMAVLinkListener {

	@FXML
	private TreeTableView<DataSet> treetableview;

	@FXML
	private TreeTableColumn<DataSet, String> message_col;

	@FXML
	private TreeTableColumn<DataSet, String> variable_col;

	@FXML
	private TreeTableColumn<DataSet, String>  value_col;


	final ObservableMap<String,Data> allData = FXCollections.observableHashMap();
	final ObservableMap<String,Data> remData = FXCollections.observableHashMap();


	public MAVInspectorTab() {
		FXMLLoadHelper.load(this, "MAVInspectorTab.fxml");
	}

	@SuppressWarnings("unchecked")
	@FXML
	private void initialize() {



		TreeItem<DataSet> root = new TreeItem<DataSet>(new DataSet("", ""));
		treetableview.setRoot(root);
		treetableview.setShowRoot(false);
		root.setExpanded(true);

		treetableview.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				treetableview.getSelectionModel().clearSelection();
			}
		});

		message_col.setCellValueFactory(param -> {
			return param.getValue().isLeaf() ? new SimpleStringProperty("") : param.getValue().getValue().strProperty();
		});

		message_col.setCellFactory(column -> {
			return new TreeTableCell<DataSet, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					if(!empty) {
						setText(item);
						setStyle("-fx-text-fill: #D0D0F0;");
					} else
						setText("");
				}
			};
		});


		variable_col.setCellValueFactory(new Callback<CellDataFeatures<DataSet, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<DataSet, String> param) {
				return param.getValue().isLeaf() ? param.getValue().getValue().strProperty() : new SimpleStringProperty("");
			}
		});

		variable_col.setCellFactory(column -> {
			return new TreeTableCell<DataSet, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					if(!empty) {
						setText(item);
						setStyle("-fx-text-fill: #80F080;");
					} else
						setText("");
				}
			};
		});

		variable_col.setSortType(SortType.ASCENDING);
		variable_col.setSortable(true);


		value_col.setCellValueFactory(new Callback<CellDataFeatures<DataSet, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<DataSet, String> param) {
				return param.getValue().getValue().getValue();
			}
		});

		value_col.setCellFactory(column -> {
			return new TreeTableCell<DataSet, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					if(!empty) {
						setText(item);
						setStyle("-fx-text-fill: #F0F080;-fx-alignment: CENTER-RIGHT;");
					} else
						setText("");
				}
			};
		});

		treetableview.setPlaceholder(new Label("Messages are shown when published"));

		treetableview.prefHeightProperty().bind(heightProperty().subtract(5));
		treetableview.prefWidthProperty().bind(widthProperty().subtract(2));

		treetableview.getSortOrder().addAll(message_col,variable_col);


		StateProperties.getInstance().getConnectedProperty().addListener((v,ov,nv) -> {
			if(!nv.booleanValue()) {
				Platform.runLater(() -> {
					allData.clear();
					treetableview.getRoot().getChildren().clear();
				});
			}
		});


		ExecutorService.get().scheduleAtFixedRate(new CleanUp(), 20, 5, TimeUnit.SECONDS);

	}

	public MAVInspectorTab setup(IMAVController control) {
		control.addMAVLinkListener(this);
		return this;
	}

	@Override
	public void received(Object msg) {
		parseMessageString(msg.toString().split("  "));	
	}

	private void parseMessageString(String[] msg) {
		String _msg = msg[0].replace(':', ' ').trim();

		if(!allData.containsKey(_msg)) {

			ObservableMap<String,DataSet> variables =  FXCollections.observableHashMap();

			for(String v : msg)
				if(v.contains("=")) {
					try {
						String[] p = v.split("=");
						variables.put(p[0].trim(), new DataSet(p[0].trim(),p[1]));
					} catch(Exception e) {
						//System.err.println(e.getMessage()+": "+v);
					}
				}

			Data data = new Data(_msg,variables);
			data.addToTree(treetableview);

			allData.put(_msg,data);

			for (DataSet dataset : data.getData().values()) {
				TreeItem<DataSet> treeItem = new TreeItem<DataSet>(dataset);
				data.ti.getChildren().add(treeItem);
			}
			Platform.runLater(() -> {
				treetableview.sort();
			});	

		} else {

			Data data = allData.get(_msg);
			if(data.updateRate()) {
				for(String v : msg) {
					if(v.contains("=")) {
						String[] p = v.split("=");
						try {
							data.getData().get(p[0].trim()).setValue(p[1]);
						} catch(Exception k) {   }
					}
				}
			}

		}

	}

	class CleanUp implements Runnable {

		@Override
		public void run() {

			if(!StateProperties.getInstance().getConnectedProperty().get() && allData.size() > 0) {
				System.out.println("Treeview cleared");
				allData.clear();
				treetableview.getRoot().getChildren().clear();
				return;
			}

			if(!isDisabled()) {

				//	Platform.runLater(() -> {

				remData.clear();
				allData.forEach((k,d) -> {
					if(d.getLastUpdate() == 0 || d.ti.isExpanded())
						return;
					if(System.currentTimeMillis() - d.getLastUpdate() > 10000) {
						remData.put(k, d);
					}
				});
				if(remData.size()>0) {
					Platform.runLater(() -> {
						remData.forEach((k,d) -> {
							d.removeFromTree(treetableview);
							allData.remove(k);
						});
						treetableview.sort();
					});
				}
				//		});

			}
		}
	}


	class Data {

		private DataSet name_set;
		private String name;
		private Map<String,DataSet> data = new HashMap<String,DataSet>();

		private float rate;
		private long  tms;
		private long  count = 0;
		private long  last_update;

		public TreeItem<DataSet> ti=null;

		public Data(String name, ObservableMap<String,DataSet> data) {
			this.name = name.substring(15);
			this.name_set = new DataSet(name.substring(15),null);
			this.data = data;
			this.tms = 0;
			this.ti = new TreeItem<>(name_set);
			this.ti.setExpanded(false);
		}

		public void addToTree(TreeTableView<DataSet> view) {
			Platform.runLater(() -> {
				view.getRoot().getChildren().add(ti);
			});
		}

		public void removeFromTree(TreeTableView<DataSet> view) {
			view.getRoot().getChildren().remove(ti);
		}

		public Map<String,DataSet> getData() {
			return data;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name.substring(15);
		}

		public DataSet getNameSet() {
			return name_set;
		}

		public long getLastUpdate() {
			return tms;
		}

		public boolean updateRate() {

			if(tms != 0 && (System.currentTimeMillis() - tms) > 0 )
				rate = (rate *  count + 1000.0f/(System.currentTimeMillis() - tms)) / ++count;
			tms = System.currentTimeMillis();

			if(isDisabled()) {
				last_update = System.currentTimeMillis();
				return false;
			}

			if((System.currentTimeMillis() - last_update) > 333 && count > 5) {
				if(rate > 1)
				    this.name_set.setStr(name+" ("+(int)(rate)+"Hz)");
				else if(rate > 0.2)
				    this.name_set.setStr(name+" ("+(int)(rate+0.5f)+"Hz)");
				last_update = System.currentTimeMillis();
				return true;
			}
			return false;
		}
	}

	class DataSet {

		StringProperty str = new SimpleStringProperty();
		StringProperty value = new SimpleStringProperty();

		public DataSet(String s, String n) {
			str.set(s);
			if(n!=null)
				value.set(n);
		}


		public StringProperty getValue() {
			return value;
		}

		public void setValue(String no) {
			this.value.set(no);
		}

		public String getStr() {
			return str.get();
		}

		public StringProperty strProperty() {
			return str;
		}

		public void setStr(String str) {
			this.str.set(str);
		}
	}
}
