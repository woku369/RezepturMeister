package at.rezepturmeister.naehrwertrechner.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toQuelle(value: String): NaehrwertQuelle = NaehrwertQuelle.valueOf(value)

    @TypeConverter
    fun fromQuelle(value: NaehrwertQuelle): String = value.name
}
