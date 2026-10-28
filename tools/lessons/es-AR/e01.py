from _common import make
groups = [
("Begrüssen & Verabschieden", """
hola|hola|hallo|int
buen_dia|buen día|guten Morgen|ph|In Argentinien sagt man meist „buen día“ (Singular).
buenas_tardes|buenas tardes|guten Tag (nachmittags)|ph
buenas_noches|buenas noches|guten Abend; gute Nacht|ph
buenas|buenas|Tag! (kurz, zu jeder Tageszeit)|int|Sehr üblich beim Betreten eines Ladens.
chau|chau|tschüss|int
hasta_luego|hasta luego|bis später|ph
hasta_manana|hasta mañana|bis morgen|ph
nos_vemos|nos vemos|wir sehen uns|ph
como_andas|¿cómo andás?|wie geht's? (zu vos)|ph|Typisch argentinisch; auch: ¿cómo estás?
que_tal|¿qué tal?|wie geht's?|ph
todo_bien|todo bien|alles gut|ph
bien|bien|gut|adv
mucho_gusto|mucho gusto|freut mich|ph|Oder: encantado / encantada.
me_llamo|me llamo|ich heisse|ph
"""),
("Höflichkeit", """
gracias|gracias|danke|int
muchas_gracias|muchas gracias|vielen Dank|ph
por_favor|por favor|bitte|ph
de_nada|de nada|gern geschehen|ph
perdon|perdón|Entschuldigung|int
disculpa|disculpá|entschuldige (zu vos)|ph|Zu usted: disculpe.
permiso|permiso|Darf ich mal? (zum Vorbeigehen)|int
si|sí|ja|adv|Mit Akzent! si ohne Akzent heisst „wenn, ob“.
no|no|nein; nicht|adv
dale|dale|okay, einverstanden|int|Argentinisch für „ok“ – hört man ständig.
"""),
("Personen & Verständigung", """
como|cómo|wie|adv
yo|yo|ich|pron|In Argentinien oft wie „scho“ ausgesprochen.
vos|vos|du|pron|In Argentinien statt tú: vos sos, vos tenés.
usted|usted|Sie (höflich)|pron
ustedes|ustedes|ihr; Sie (Plural)|pron|In Argentinien gibt es kein vosotros.
un_poco|un poco|ein bisschen|ph
hablar|hablar|sprechen|v
entender|entender|verstehen|v|yo entiendo, vos entendés
despacio|despacio|langsam|adv
espanol|español|Spanisch|n|Viele Argentinier sagen „castellano“.
aleman|alemán|Deutsch (Sprache); deutsch|n
"""),
]
sentences = [
("¡Hola! ¿Cómo andás?","Hallo! Wie geht's?","hola como_andas","voseo"),
("Buen día, ¿cómo está usted?","Guten Morgen, wie geht es Ihnen?","buen_dia como usted","vos_usted"),
("Buenas tardes, señora.","Guten Tag, meine Dame.","buenas_tardes"),
("Buenas noches, hasta mañana.","Gute Nacht, bis morgen.","buenas_noches hasta_manana"),
("¡Buenas! ¿Todo bien?","Tag! Alles gut?","buenas todo_bien"),
("Todo bien, ¿y vos?","Alles gut, und dir?","todo_bien vos","voseo"),
("Bien, gracias.","Gut, danke.","bien gracias"),
("¿Qué tal? Bien, ¿y usted?","Wie geht's? Gut, und Ihnen?","que_tal bien usted","vos_usted"),
("Me llamo Pedro. Mucho gusto.","Ich heisse Pedro. Freut mich.","me_llamo mucho_gusto"),
("Mucho gusto, yo soy Ana.","Freut mich, ich bin Ana.","mucho_gusto yo"),
("Muchas gracias. – De nada.","Vielen Dank. – Gern geschehen.","muchas_gracias de_nada"),
("Un café, por favor.","Einen Kaffee, bitte.","por_favor"),
("Perdón, no entiendo.","Entschuldigung, ich verstehe nicht.","perdon no entender","negacion"),
("Disculpá, ¿hablás alemán?","Entschuldige, sprichst du Deutsch?","disculpa hablar aleman","voseo"),
("Disculpe, ¿habla usted español?","Entschuldigen Sie, sprechen Sie Spanisch?","usted hablar espanol","vos_usted"),
("Hablo un poco de español.","Ich spreche ein bisschen Spanisch.","hablar un_poco espanol"),
("No hablo bien español.","Ich spreche nicht gut Spanisch.","no hablar bien espanol","negacion"),
("¿Podés hablar más despacio, por favor?","Kannst du bitte langsamer sprechen?","hablar despacio por_favor","voseo"),
("Más despacio, por favor.","Langsamer, bitte.","despacio por_favor"),
("Permiso, ¿puedo pasar?","Darf ich mal vorbei?","permiso"),
("Sí, claro.","Ja, klar.","si"),
("No, gracias.","Nein, danke.","no gracias","negacion"),
("¿Vamos? – ¡Dale!","Gehen wir? – Okay!","dale"),
("Chau, hasta luego.","Tschüss, bis später.","chau hasta_luego"),
("Nos vemos mañana.","Wir sehen uns morgen.","nos_vemos"),
("¿Ustedes hablan alemán?","Sprecht ihr Deutsch?","ustedes hablar aleman","vos_usted"),
("Yo no entiendo nada.","Ich verstehe gar nichts.","yo no entender","negacion"),
("¿Entendés? – Sí, entiendo.","Verstehst du? – Ja, ich verstehe.","entender si","voseo"),
("¿Cómo se dice esto en español?","Wie sagt man das auf Spanisch?","como espanol"),
("Vos sos muy amable.","Du bist sehr nett.","vos","voseo"),
("Perdón, ¿cómo te llamás?","Entschuldigung, wie heisst du?","perdon como","voseo"),
]
grammar = [
 {"id":"voseo","title":"vos – das argentinische Du",
  "explanation":"In Argentinien sagt man vos statt tú. Die Verbformen sind auf der Endung betont und tragen dort einen Akzent:\n\n• hablar → vos hablás\n• tener → vos tenés\n• querer → vos querés\n• poder → vos podés\n• ser → vos sos (!)\n\nAuch die Befehlsform ist anders: hablá, vení, decime, disculpá.\nObjektpronomen bleibt te: ¿Cómo te llamás?\nNach Präpositionen: con vos, para vos.",
  "examples":[{"text":"¿Vos sos de acá?","de":"Bist du von hier?"},{"text":"¿Tenés tiempo?","de":"Hast du Zeit?"},{"text":"Vení, por favor.","de":"Komm bitte."}]},
 {"id":"vos_usted","title":"vos, usted, ustedes",
  "explanation":"• vos – du (Freunde, Gleichaltrige, im Alltag sehr üblich)\n• usted – Sie (Ältere, Behörden, formell). Das Verb steht wie bei er/sie: usted habla, usted tiene.\n• ustedes – ihr UND Sie (Plural). Das spanische vosotros gibt es in Argentinien nicht.\n\nIm Zweifel: Argentinier duzen schnell. Wer mit usted anfängt, hört oft „tratame de vos“ (sag du zu mir).",
  "examples":[{"text":"¿Usted es el señor García?","de":"Sind Sie Herr García?"},{"text":"¿Ustedes son de Alemania?","de":"Seid ihr / Sind Sie aus Deutschland?"}]},
 {"id":"negacion","title":"Verneinung mit no",
  "explanation":"Man stellt einfach no vor das Verb:\n• Hablo español. → No hablo español.\n\nAnders als im Deutschen bleibt die Verneinung doppelt:\n• No entiendo nada. – Ich verstehe nichts.\n• No hay nadie. – Es ist niemand da.",
  "examples":[{"text":"No entiendo.","de":"Ich verstehe nicht."},{"text":"No tengo nada.","de":"Ich habe nichts."}]},
]
wg = {"vos":["voseo","vos_usted"],"usted":["vos_usted"],"ustedes":["vos_usted"],"no":["negacion"]}
make('01_saludos.json','es-AR-01',1,'Begrüssung & Höflichkeit','Saludos y cortesía',groups,sentences,grammar,wg)
