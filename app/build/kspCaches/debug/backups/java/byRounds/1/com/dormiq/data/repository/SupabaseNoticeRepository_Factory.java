package com.dormiq.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SupabaseNoticeRepository_Factory implements Factory<SupabaseNoticeRepository> {
  private final Provider<SupabaseClient> supabaseProvider;

  public SupabaseNoticeRepository_Factory(Provider<SupabaseClient> supabaseProvider) {
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public SupabaseNoticeRepository get() {
    return newInstance(supabaseProvider.get());
  }

  public static SupabaseNoticeRepository_Factory create(Provider<SupabaseClient> supabaseProvider) {
    return new SupabaseNoticeRepository_Factory(supabaseProvider);
  }

  public static SupabaseNoticeRepository newInstance(SupabaseClient supabase) {
    return new SupabaseNoticeRepository(supabase);
  }
}
