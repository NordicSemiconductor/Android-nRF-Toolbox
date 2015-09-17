/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.uart;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.uart.domain.UartConfiguration;

public class UARTConfigurationsAdapter extends WearableListView.Adapter {
	private final LayoutInflater mInflater;
	private List<UartConfiguration> mConfigurations;

	public UARTConfigurationsAdapter(final Context context) {
		mInflater = LayoutInflater.from(context);
	}

	/**
	 * Populates the adapter with list of configurations.
	 */
	public void setConfigurations(final List<UartConfiguration> configurations) {
		mConfigurations = configurations;
		notifyDataSetChanged();
	}

	@Override
	public WearableListView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
		return new ConfigurationViewHolder(mInflater.inflate(R.layout.configuration_item, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(final WearableListView.ViewHolder holder, final int position) {
		final ConfigurationViewHolder viewHolder = (ConfigurationViewHolder) holder;
		viewHolder.setConfiguration(mConfigurations.get(position));
	}

	@Override
	public int getItemCount() {
		return mConfigurations != null ? mConfigurations.size() : 0;
	}

	public static class ConfigurationViewHolder extends WearableListView.ViewHolder {
		private UartConfiguration mConfiguration;
		private TextView mName;

		public ConfigurationViewHolder(final View itemView) {
			super(itemView);

			mName = (TextView) itemView.findViewById(R.id.name);
		}

		private void setConfiguration(final UartConfiguration configuration) {
			mConfiguration = configuration;
			mName.setText(configuration.getName());
		}

		public UartConfiguration getConfiguration() {
			return mConfiguration;
		}
	}
}
