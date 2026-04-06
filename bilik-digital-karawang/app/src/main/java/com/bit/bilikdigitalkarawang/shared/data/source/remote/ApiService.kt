package com.bit.bilikdigitalkarawang.shared.data.source.remote

import com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto.LoginResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.KandidatResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.ListVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PemilihResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.SyncRekapResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.SyncRowResponse
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.ResponseBody
import retrofit2.Response

interface ApiService {
    @Multipart
    @POST("login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("device_id") deviceId: RequestBody,
    ): LoginResponse

    @GET("kandidats")
    suspend fun getKandidat(
        @Header("Authorization") authorization: String
    ): KandidatResponse

    @GET("pemilih")
    suspend fun getPemilih(
        @Header("Authorization") authorization: String
    ): PemilihResponse

    @GET("check-connection")
    suspend fun checkConnection(): Response<ResponseBody>

    @Multipart
    @POST("rekap_hasil_pemilihan")
    suspend fun syncRekap(
        @Header("Authorization") authorization: String,
        @Part("jml_partisipasi_suara") jumlahPartisipasi: RequestBody,
        @Part("suara_sah") suaraSah: RequestBody,
        @Part("suara_tidak_sah") suaraTidakSah: RequestBody,
        @Part("suara_abstain") suaraAbstain: RequestBody,
        @Part("jml_suara_kandidat_no_urut_1") jumlahPemilihNoUrut1: RequestBody,
        @Part("jml_suara_kandidat_no_urut_2") jumlahPemilihNoUrut2: RequestBody,
        @Part("jml_suara_kandidat_no_urut_3") jumlahPemilihNoUrut3: RequestBody,
        @Part("jml_suara_kandidat_no_urut_4") jumlahPemilihNoUrut4: RequestBody,
        @Part("jml_suara_kandidat_no_urut_5") jumlahPemilihNoUrut5: RequestBody,
        @Part("jml_suara_kandidat_no_urut_6") jumlahPemilihNoUrut6: RequestBody,
        @Part("jml_suara_kandidat_no_urut_7") jumlahPemilihNoUrut7: RequestBody,
        @Part("jml_suara_kandidat_no_urut_8") jumlahPemilihNoUrut8: RequestBody,
    ): SyncRekapResponse

    @Multipart
    @POST("votes")
    suspend fun syncRow(
        @Header("Authorization") authorization: String,
        @Part("votes") votes: RequestBody,
    ): SyncRowResponse

    @Multipart
    @POST("vote")
    suspend fun postVote(
        @Header("Authorization") authorization: String,
        @Part("id_dpt") idDpt: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("no_urut") noUrut: RequestBody,
        @Part("status") status: RequestBody,
    ): PostVoteResponse

    @Multipart
    @POST("get_list_votes")
    suspend fun getListVote(
        @Header("Authorization") authorization: String,
        @Part("device_id") deviceId: RequestBody,
    ): ListVoteResponse

}