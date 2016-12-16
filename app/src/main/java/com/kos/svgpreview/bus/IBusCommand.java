package com.kos.svgpreview.bus;

import com.kos.svgpreview.data.BasicData;

/**
 * Created by Kos on 28.09.2016.
 */

public interface IBusCommand {
	void openFromCommand(String fileName,int command ,boolean addBack);
	void cancelNavigateStack(int command ,boolean addBack);
}
