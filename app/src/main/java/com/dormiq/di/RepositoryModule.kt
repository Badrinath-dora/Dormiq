package com.dormiq.di

import com.dormiq.data.repository.*
import com.dormiq.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindHostelRepository(impl: SupabaseHostelRepository): HostelRepository

    @Binds @Singleton
    abstract fun bindFloorRepository(impl: SupabaseFloorRepository): FloorRepository

    @Binds @Singleton
    abstract fun bindRoomRepository(impl: SupabaseRoomRepository): RoomRepository

    @Binds @Singleton
    abstract fun bindBedRepository(impl: SupabaseBedRepository): BedRepository

    @Binds @Singleton
    abstract fun bindTenantRepository(impl: SupabaseTenantRepository): TenantRepository

    @Binds @Singleton
    abstract fun bindIssueRepository(impl: SupabaseIssueRepository): IssueRepository

    @Binds @Singleton
    abstract fun bindNoticeRepository(impl: SupabaseNoticeRepository): NoticeRepository

    @Binds @Singleton
    abstract fun bindPaymentRepository(impl: SupabasePaymentRepository): PaymentRepository

    @Binds @Singleton
    abstract fun bindGalleryRepository(impl: SupabaseGalleryRepository): GalleryRepository

    @Binds @Singleton
    abstract fun bindFoodMenuRepository(impl: SupabaseFoodMenuRepository): FoodMenuRepository
}
