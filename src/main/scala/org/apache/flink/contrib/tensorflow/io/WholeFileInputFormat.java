package org.apache.flink.contrib.tensorflow.io;

import org.apache.flink.api.common.io.BinaryInputFormat;
import org.apache.flink.api.common.io.FileInputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileInputSplit;
import org.apache.flink.core.fs.FileStatus;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eron Wright
 */
public abstract class WholeFileInputFormat extends FileInputFormat<Row> {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(WholeFileInputFormat.class);

	public WholeFileInputFormat(Path filePath) {
		super(filePath);
		this.unsplittable = true;
	}

	@Override
	public void configure(Configuration parameters) {
		super.configure(parameters);
	}



	@Override
	protected boolean testForUnsplittable(FileStatus pathFile) {
		return true;
	}


	//	protected List<FileStatus> getFiles() throws IOException {
//		// get all the files that are involved in the splits
//		List<FileStatus> files = new ArrayList<FileStatus>();
//
//		final FileSystem fs = this.filePath.getFileSystem();
//		final FileStatus pathFile = fs.getFileStatus(this.filePath);
//
//		if (pathFile.isDir()) {
//			// input is directory. list all contained files
//			final FileStatus[] partials = fs.listStatus(this.filePath);
//			for (FileStatus partial : partials) {
//				if (!partial.isDir()) {
//					files.add(partial);
//				}
//			}
//		} else {
//			files.add(pathFile);
//		}
//
//		return files;
//	}
}
