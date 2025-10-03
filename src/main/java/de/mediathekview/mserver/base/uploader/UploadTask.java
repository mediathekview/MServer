package de.mediathekview.mserver.base.uploader;

import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.ProgressListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public abstract class UploadTask<T extends UploadTarget> implements Runnable {
  protected final Collection<ProgressListener> progressListeners;
  protected final Collection<MessageListener> messageListeners;
  protected final Path sourcePath;
  protected final T uploadTarget;

  protected UploadTask(final Path aSourcePath, final T aUploadTarget) {
    progressListeners = new ArrayList<>();
    messageListeners = new ArrayList<>();
    sourcePath = aSourcePath;
    uploadTarget = aUploadTarget;
  }

  public boolean addAllProgressListener(final Collection<? extends ProgressListener> c) {
    return progressListeners.addAll(c);
  }

  public boolean addAllMessageListener(final Collection<? extends MessageListener> c) {
    return messageListeners.addAll(c);
  }

  protected abstract void upload();

  @Override
  public void run() {
    upload();
  }
}
