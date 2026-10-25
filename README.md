# Efor Takip — TechnoCamp Proje Ödevi

Müşteri temsilcilerinin haftalık mesai/mola/mazeret bilgilerini girdiği, yöneticilerin de bu kayıtları filtreleyip Excel olarak indirebildiği bir web uygulaması.

| Katman   | Teknoloji                                                  |
|----------|------------------------------------------------------------|
| Backend  | Java 21, Spring Boot 3.5 (Web, Data JPA, Security, Validation), Apache POI |
| Frontend | Angular 22, Angular Material (datepicker, timepicker, table) |
| Veritabanı | Oracle (prod) · H2 Oracle modu (kurulumsuz yerel geliştirme) |

## Özellikler

**Performans Girişi** (tüm kullanıcılar)
- Hafta bazlı görünüm: önceki/sonraki hafta, "Bu hafta" kısayolu, haftalık özet (çalışılan gün, net çalışma, mola, mazeret)
- Kayıt ekleme: gün **datepicker**, mesai başlangıç/bitiş **saat seçici**, mola **sayı**, mazeret **dropdown**, mazeret saati
- Satır içi düzenleme ve silme, çoklu seçimle toplu silme (onay penceresiyle)
- Doğrulamalar: bitiş > başlangıç, mola ≤ mesai süresi, mazeret seçildiyse saati zorunlu (hem frontend hem backend)
- Temsilci yalnızca kendi kayıtlarını görür/değiştirir

**Performans Raporu** (yalnızca ADMIN)
- Müşteri temsilcisi ve tarih aralığı filtreleri
- Sıralanabilir, sayfalı tablo ve özet kartları
- Ekrandaki filtrelerle aynı sonucu **Excel (.xlsx)** olarak indirme
- Hem menü/route guard'ı hem de backend (`/api/reports/**` → `ROLE_ADMIN`) düzeyinde korunur

## Hızlı başlangıç

Gereksinimler: JDK 21+, Node.js 22.22+ veya 24.15+.

```bash
# 1) Backend (http://localhost:8080) — H2 bellek içi veritabanı + demo verisi
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 2) Frontend (http://localhost:4200) — /api istekleri 8080'e proxy'lenir
cd frontend
npm install
npm start
```

Demo kullanıcıları (`DataSeeder` tarafından oluşturulur):

| Kullanıcı | Şifre       | Rol                 |
|-----------|-------------|---------------------|
| `admin`   | `admin123`  | Yönetici (rapor ekranı) |
| `ayse`    | `ayse123`   | Müşteri temsilcisi  |
| `mehmet`  | `mehmet123` | Müşteri temsilcisi  |

## Oracle ile çalıştırma

1. DBeaver ile Oracle'a bağlanıp [db/oracle/schema.sql](db/oracle/schema.sql) betiğini çalıştırın (Oracle 12c+).
2. Backend'i `oracle` profiliyle başlatın:

```bash
cd backend
DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1 DB_USERNAME=efor DB_PASSWORD=efor \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=oracle
```

`SEED_DEMO_DATA=false` verilirse demo kullanıcıları oluşturulmaz.

## Veri modeli

`PERFORMANCE` tablosu ödevde verilen kolonlarla birebir aynıdır:

| Kolon | Tip | Açıklama |
|---|---|---|
| ID | NUMBER(38,0) | Identity |
| AGENTID | VARCHAR2(100) | Temsilci sicili |
| FIRSTNAME / SURNAME | VARCHAR2(100) | Temsilci adı |
| BEGIN / END | TIMESTAMP | Mesai başlangıç / bitiş |
| DATEINFO | DATE | Gün |
| EXCUSE | VARCHAR2(100) | Mazeret |
| EXCUSEHOURS | NUMBER(38,0) | Mazeret saati |
| TIMEOUT | NUMBER(38,0) | Kullanılan mola (saat) |

Kullanıcılar `APP_USER` tablosunda (BCrypt şifre, `AGENT`/`ADMIN` rolü) tutulur.

## REST API

| Metot | Yol | Açıklama |
|---|---|---|
| GET | `/api/auth/me` | Giriş yapan kullanıcı |
| GET | `/api/excuses` | Mazeret seçenekleri |
| GET | `/api/performances?from&to` | Kendi kayıtlarım |
| POST | `/api/performances` | Kayıt ekle |
| PUT | `/api/performances/{id}` | Kayıt güncelle |
| DELETE | `/api/performances/{id}` · `/api/performances?ids=1,2` | Tekli / toplu sil |
| GET | `/api/reports/agents` | Temsilci listesi (ADMIN) |
| GET | `/api/reports/performances?agentId&from&to` | Rapor (ADMIN) |
| GET | `/api/reports/performances/export?agentId&from&to` | Excel indir (ADMIN) |

Kimlik doğrulama durumsuz HTTP Basic ile yapılır; hatalar `{"message": "..."}` biçiminde döner.

## Testler

```bash
cd backend && ./mvnw test           # API entegrasyon testleri (MockMvc + H2)
cd frontend && npx ng test --watch=false
```
