package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.NoticeRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class NoticeViewModel_Factory implements Factory<NoticeViewModel> {
  private final Provider<NoticeRepository> repositoryProvider;

  public NoticeViewModel_Factory(Provider<NoticeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public NoticeViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static NoticeViewModel_Factory create(Provider<NoticeRepository> repositoryProvider) {
    return new NoticeViewModel_Factory(repositoryProvider);
  }

  public static NoticeViewModel newInstance(NoticeRepository repository) {
    return new NoticeViewModel(repository);
  }
}
