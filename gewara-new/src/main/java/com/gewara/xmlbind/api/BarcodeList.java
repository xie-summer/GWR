package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.machine.Barcode;

public class BarcodeList {
	private List<Barcode> barcodeList = new ArrayList<Barcode>();

	public List<Barcode> getBarcodeList() {
		return barcodeList;
	}

	public void setBarcodeList(List<Barcode> barcodeList) {
		this.barcodeList = barcodeList;
	}
	public void addBarcode(Barcode barcode){
		this.barcodeList.add(barcode);
	}
}
