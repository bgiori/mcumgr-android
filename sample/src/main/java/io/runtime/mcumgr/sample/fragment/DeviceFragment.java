/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.runtime.mcumgr.sample.R;

public class DeviceFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
							 @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_device, container, false);
	}
}
