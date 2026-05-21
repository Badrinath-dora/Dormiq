package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.IssueRepository;
import com.dormiq.domain.repository.NoticeRepository;
import com.dormiq.domain.repository.RoomRepository;
import com.dormiq.domain.repository.TenantRepository;
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
public final class TenantViewModel_Factory implements Factory<TenantViewModel> {
  private final Provider<TenantRepository> tenantRepositoryProvider;

  private final Provider<RoomRepository> roomRepositoryProvider;

  private final Provider<IssueRepository> issueRepositoryProvider;

  private final Provider<NoticeRepository> noticeRepositoryProvider;

  public TenantViewModel_Factory(Provider<TenantRepository> tenantRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<IssueRepository> issueRepositoryProvider,
      Provider<NoticeRepository> noticeRepositoryProvider) {
    this.tenantRepositoryProvider = tenantRepositoryProvider;
    this.roomRepositoryProvider = roomRepositoryProvider;
    this.issueRepositoryProvider = issueRepositoryProvider;
    this.noticeRepositoryProvider = noticeRepositoryProvider;
  }

  @Override
  public TenantViewModel get() {
    return newInstance(tenantRepositoryProvider.get(), roomRepositoryProvider.get(), issueRepositoryProvider.get(), noticeRepositoryProvider.get());
  }

  public static TenantViewModel_Factory create(Provider<TenantRepository> tenantRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<IssueRepository> issueRepositoryProvider,
      Provider<NoticeRepository> noticeRepositoryProvider) {
    return new TenantViewModel_Factory(tenantRepositoryProvider, roomRepositoryProvider, issueRepositoryProvider, noticeRepositoryProvider);
  }

  public static TenantViewModel newInstance(TenantRepository tenantRepository,
      RoomRepository roomRepository, IssueRepository issueRepository,
      NoticeRepository noticeRepository) {
    return new TenantViewModel(tenantRepository, roomRepository, issueRepository, noticeRepository);
  }
}
