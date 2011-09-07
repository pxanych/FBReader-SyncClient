/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.plugin.synchronization;

import java.util.*;

import android.content.*;
import android.net.Uri;

import org.geometerplus.android.fbreader.api.PluginApi;

import org.geometerplus.fbreader.plugin.synchronization.R;
import org.geometerplus.fbreader.plugin.synchronization.service.FBSyncPositionsService;

public class PluginInfo extends PluginApi.PluginInfo {
	@Override
	protected List<PluginApi.ActionInfo> implementedActions(Context context) {
		
		Intent intent = new Intent(context, FBSyncPositionsService.class);
		intent.putExtra(
				FBSyncPositionsService.FBREADER_ACTION, 
				FBSyncPositionsService.FBREADER_STARTED);
		context.startService(intent);
		
		return Collections.<PluginApi.ActionInfo>singletonList(new PluginApi.MenuActionInfo(
			Uri.parse("http://sync.com/sync"),
			context.getText(R.string.sync_menu_item).toString()
		));
	}
}
