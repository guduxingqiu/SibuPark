package sibu.park.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sibu.park.data.repository.AuthRepository
import sibu.park.data.repository.CouponRepository
import sibu.park.data.repository.FineRepository
import sibu.park.data.repository.ReportRepository
import sibu.park.data.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepository(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideCouponRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CouponRepository {
        return CouponRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideFineRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FineRepository {
        return FineRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideReportRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ReportRepository {
        return ReportRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): TransactionRepository {
        return TransactionRepository(firestore, auth)
    }
} 