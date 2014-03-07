/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
 * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package eu.spaziodati.batchrefine.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

import org.apache.tools.tar.TarOutputStream;

import com.google.refine.ProjectManager;
import com.google.refine.ProjectMetadata;
import com.google.refine.history.HistoryEntry;
import com.google.refine.history.HistoryEntryManager;
import com.google.refine.model.Project;

/**
 * BatchRefine does not operate with projects, as it transforms individual
 * files. Yet, the refine engine requires a {@link ProjectManager} to be
 * present. This stub class satisfies basic engine needs while doing the least
 * amount of work possible.
 */
public class ProjectManagerStub extends ProjectManager {

	public synchronized static void initialize() {
		singleton = new ProjectManagerStub();
	}

	private final HistoryEntryManagerStub fHistoryManager = new HistoryEntryManagerStub();

	@Override
	public boolean loadProjectMetadata(long projectID) {
		return false;
	}

	@Override
	protected Project loadProject(long id) {
		return null;
	}

	@Override
	public void importProject(long projectID, InputStream inputStream,
			boolean gziped) throws IOException {
	}

	@Override
	public void exportProject(long projectId, TarOutputStream tos)
			throws IOException {
	}

	@Override
	protected void saveMetadata(ProjectMetadata metadata, long projectId)
			throws Exception {
	}

	@Override
	protected void saveProject(Project project) throws IOException {
	}

	@Override
	protected void saveWorkspace() {
	}

	@Override
	public HistoryEntryManager getHistoryEntryManager() {
		return fHistoryManager;
	}

	@Override
	public void deleteProject(long projectID) {
	}

	private static class HistoryEntryManagerStub implements HistoryEntryManager {

		@Override
		public void loadChange(HistoryEntry historyEntry) {
		}

		@Override
		public void saveChange(HistoryEntry historyEntry) throws Exception {
		}

		@Override
		public void save(HistoryEntry historyEntry, Writer writer,
				Properties options) {
		}

		@Override
		public void delete(HistoryEntry historyEntry) {
		}

	}
}
