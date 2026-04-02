package com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto


import com.google.gson.annotations.SerializedName

data class SyncRekapDto(
    @SerializedName("id_tps")
    val idTps: String = "",
    @SerializedName("jml_kandidat")
    val jmlKandidat: Int = 0,
    @SerializedName("jml_partisipasi_suara")
    val jmlPartisipasiSuara: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_1")
    val jmlSuaraKandidatNoUrut1: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_2")
    val jmlSuaraKandidatNoUrut2: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_3")
    val jmlSuaraKandidatNoUrut3: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_4")
    val jmlSuaraKandidatNoUrut4: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_5")
    val jmlSuaraKandidatNoUrut5: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_6")
    val jmlSuaraKandidatNoUrut6: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_7")
    val jmlSuaraKandidatNoUrut7: String = "",
    @SerializedName("jml_suara_kandidat_no_urut_8")
    val jmlSuaraKandidatNoUrut8: String = "",
    @SerializedName("kode_kab")
    val kodeKab: String = "",
    @SerializedName("kode_kec")
    val kodeKec: String = "",
    @SerializedName("kode_kel")
    val kodeKel: String = "",
    @SerializedName("kode_pro")
    val kodePro: String = "",
    @SerializedName("suara_abstain")
    val suaraAbstain: String = "",
    @SerializedName("suara_sah")
    val suaraSah: String = "",
    @SerializedName("suara_tidak_sah")
    val suaraTidakSah: String = "",
    @SerializedName("updated_at")
    val updatedAt: String = ""
)