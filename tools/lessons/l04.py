from lib import make
groups = [
("Ankommen & Einchecken", """
hotel|o hotel|das Hotel|n
pousada|a pousada|die Pension, das Gästehaus|n|Typisch an der Küste und auf dem Land.
albergue|o albergue|das Hostel, die Jugendherberge|n
hospedagem|a hospedagem|die Unterkunft|n
recepcao|a recepção|die Rezeption|n
check_in|o check-in|der Check-in|n
check_out|o check-out|der Check-out|n
passaporte|o passaporte|der Reisepass|n
identidade|a identidade|der Ausweis|n|Kurz für „carteira de identidade“.
hospede|o hóspede|der Gast (im Hotel)|n
noite|a noite|die Nacht; der Abend|n|à noite = abends, nachts
diaria|a diária|der Preis pro Nacht|n|Wörtlich „Tagessatz“.
chave|a chave|der Schlüssel|n
confirmar|confirmar|bestätigen|v
"""),
("Das Zimmer", """
quarto|o quarto|das Zimmer|n
quarto_solteiro|o quarto de solteiro|das Einzelzimmer|n
quarto_casal|o quarto de casal|das Doppelzimmer (Doppelbett)|n|Mit zwei Einzelbetten: quarto duplo.
cama|a cama|das Bett|n
travesseiro|o travesseiro|das Kopfkissen|n
cobertor|o cobertor|die Bettdecke|n
toalha|a toalha|das Handtuch|n
chuveiro|o chuveiro|die Dusche|n
pia|a pia|das Waschbecken|n
ar_condicionado|o ar-condicionado|die Klimaanlage|n
ventilador|o ventilador|der Ventilator|n
janela|a janela|das Fenster|n
porta|a porta|die Tür|n
varanda|a varanda|der Balkon|n
vista|a vista|die Aussicht|n|vista para o mar = Meerblick
andar|o andar|das Stockwerk|n|o térreo = das Erdgeschoss
elevador|o elevador|der Aufzug|n
escada|a escada|die Treppe|n
"""),
("Service & Ausstattung", """
wifi|o wi-fi|das WLAN|n
senha|a senha|das Passwort|n
piscina|a piscina|das Schwimmbad, der Pool|n
estacionamento|o estacionamento|der Parkplatz|n
cofre|o cofre|der Safe|n
limpeza|a limpeza|die Reinigung, das Putzen|n
lavanderia|a lavanderia|die Wäscherei|n
tomada|a tomada|die Steckdose|n|In Brasilien meist Typ N; ein Adapter ist nützlich.
carregador|o carregador|das Ladegerät|n
"""),
("Probleme", """
problema|o problema|das Problem|n|Männlich, obwohl auf -a!
quebrado|quebrado|kaputt|adj
sujo|sujo|schmutzig|adj
limpo|limpo|sauber|adj
barulhento|barulhento|laut (lärmig)|adj
funcionar|funcionar|funktionieren|v
trocar|trocar|wechseln, tauschen|v
"""),
("Fragewörter & Verben", """
onde|onde|wo|adv
quando|quando|wann|adv
qual|qual|welche(r, s); was|pron|Plural: quais
o_que|o que|was|pron
por_que|por que|warum|adv|Antwort: porque = weil
quem|quem|wer|pron
ter|ter|haben|v
precisar|precisar de|brauchen|v
ficar|ficar|bleiben; sich befinden|v|Onde fica …? = Wo ist …?
chegar|chegar|ankommen|v
sair|sair|hinausgehen; abreisen|v
"""),
]
sentences = [
("Eu tenho uma reserva no nome de Müller.","Ich habe eine Reservierung auf den Namen Müller.","eu reserva ter","ter_haben"),
("Vocês têm um quarto livre para hoje?","Habt ihr ein freies Zimmer für heute?","quarto hoje ter","ter_haben"),
("Quanto é a diária?","Wie viel kostet eine Nacht?","diaria","fragewoerter"),
("A diária inclui o café da manhã?","Ist das Frühstück im Preis inbegriffen?","diaria cafe_da_manha"),
("Um quarto de casal para três noites, por favor.","Ein Doppelzimmer für drei Nächte, bitte.","quarto_casal num_3 noite por_favor"),
("Eu prefiro um quarto de solteiro com vista para o mar.","Ich hätte lieber ein Einzelzimmer mit Meerblick.","eu quarto_solteiro vista com"),
("Posso ver o seu passaporte?","Darf ich Ihren Pass sehen?","passaporte"),
("Aqui está a chave do quarto.","Hier ist der Zimmerschlüssel.","chave quarto"),
("O quarto fica no terceiro andar.","Das Zimmer ist im dritten Stock.","quarto ficar andar"),
("Onde fica o elevador?","Wo ist der Aufzug?","onde ficar elevador","fragewoerter"),
("A que horas é o check-out?","Um wie viel Uhr ist der Check-out?","check_out hora","fragewoerter"),
("O check-in é a partir das duas da tarde.","Der Check-in ist ab zwei Uhr nachmittags.","check_in num_2"),
("Qual é a senha do wi-fi?","Wie lautet das WLAN-Passwort?","qual senha wifi","fragewoerter"),
("O wi-fi não funciona.","Das WLAN funktioniert nicht.","wifi nao funcionar"),
("O ar-condicionado está quebrado.","Die Klimaanlage ist kaputt.","ar_condicionado quebrado"),
("O chuveiro não tem água quente.","Die Dusche hat kein warmes Wasser.","chuveiro nao agua quente ter"),
("Pode trocar as toalhas, por favor?","Können Sie bitte die Handtücher wechseln?","trocar toalha por_favor"),
("Preciso de mais um travesseiro.","Ich brauche noch ein Kopfkissen.","precisar travesseiro","precisar_de"),
("Tem um cobertor extra?","Gibt es eine zusätzliche Decke?","cobertor","ter_haben"),
("O quarto está sujo.","Das Zimmer ist schmutzig.","quarto sujo"),
("O banheiro está muito limpo.","Das Bad ist sehr sauber.","banheiro muito limpo"),
("A rua é muito barulhenta à noite.","Die Strasse ist nachts sehr laut.","muito barulhento noite"),
("Posso trocar de quarto?","Kann ich das Zimmer wechseln?","trocar quarto"),
("Tem piscina no hotel?","Hat das Hotel einen Pool?","piscina hotel","ter_haben"),
("O estacionamento é gratuito?","Ist der Parkplatz kostenlos?","estacionamento"),
("Tem cofre no quarto?","Gibt es einen Safe im Zimmer?","cofre quarto","ter_haben"),
("Onde tem uma tomada?","Wo gibt es eine Steckdose?","onde tomada","fragewoerter"),
("Esqueci meu carregador.","Ich habe mein Ladegerät vergessen.","carregador"),
("Quando é a limpeza do quarto?","Wann wird das Zimmer geputzt?","quando limpeza quarto","fragewoerter"),
("O hotel tem lavanderia?","Hat das Hotel eine Wäscherei?","hotel lavanderia ter"),
("Quem fala inglês aqui?","Wer spricht hier Englisch?","quem falar","fragewoerter"),
("Por que a janela não abre?","Warum geht das Fenster nicht auf?","por_que janela nao","fragewoerter"),
("Pode fechar a porta, por favor?","Kannst du bitte die Tür schliessen?","porta por_favor"),
("O quarto tem varanda?","Hat das Zimmer einen Balkon?","quarto varanda ter"),
("A cama é muito confortável.","Das Bett ist sehr bequem.","cama muito"),
("O ventilador faz muito barulho.","Der Ventilator macht viel Lärm.","ventilador muito"),
("Eu vou sair amanhã cedo.","Ich reise morgen früh ab.","eu sair amanha"),
("Nós vamos chegar tarde à noite.","Wir kommen spät abends an.","chegar noite"),
("Quero confirmar a minha reserva.","Ich möchte meine Reservierung bestätigen.","confirmar reserva"),
("É uma pousada pequena e simpática.","Es ist eine kleine, sympathische Pension.","pousada"),
("O albergue é barato e fica perto da praia.","Das Hostel ist günstig und liegt nahe am Strand.","albergue barato ficar"),
("O que tem perto do hotel?","Was gibt es in der Nähe des Hotels?","o_que hotel","fragewoerter ter_haben"),
("Tem algum problema com o quarto?","Gibt es ein Problem mit dem Zimmer?","problema quarto com"),
("A pia está entupida.","Das Waschbecken ist verstopft.","pia"),
("A escada fica à direita.","Die Treppe ist rechts.","escada ficar"),
("Vou ficar duas noites.","Ich bleibe zwei Nächte.","ficar num_2 noite"),
("Preciso da identidade de todos os hóspedes.","Ich brauche die Ausweise aller Gäste.","identidade hospede","precisar_de"),
("Recepção, bom dia!","Rezeption, guten Morgen!","recepcao bom_dia"),
("Essa hospedagem é muito boa.","Diese Unterkunft ist sehr gut.","hospedagem muito"),
("O hóspede do quarto dez saiu.","Der Gast aus Zimmer zehn ist abgereist.","hospede quarto num_10"),
]
grammar = [
 {"id":"fragewoerter","title":"Fragewörter",
  "explanation":"• onde? – wo?  (Onde fica …? – Wo ist …?)\n• quando? – wann?\n• quanto/quanta? – wie viel?  quantos/quantas? – wie viele?\n• qual?/quais? – welche(r)? – auch für „was“ bei Auswahl: Qual é a senha?\n• o que? – was?\n• quem? – wer?\n• como? – wie?\n• por que? – warum?  Die Antwort beginnt mit porque (weil).\n\nOft hängt man „é que“ an, das klingt natürlich: Onde é que fica? O que é que você quer?",
  "examples":[{"text":"Onde fica a recepção?","de":"Wo ist die Rezeption?"},{"text":"Quantas noites?","de":"Wie viele Nächte?"},{"text":"Por que não? Porque está quebrado.","de":"Warum nicht? Weil es kaputt ist."}]},
 {"id":"ter_haben","title":"ter – haben, und tem – es gibt",
  "explanation":"ter (haben) im Präsens:\n• eu tenho – ich habe\n• você/ele/ela tem – du hast, er/sie hat\n• nós temos – wir haben\n• vocês/eles/elas têm – ihr habt, sie haben\n\nIn Brasilien sagt man für „es gibt“ meistens einfach tem (statt dem formelleren há):\nTem piscina? – Gibt es einen Pool?",
  "examples":[{"text":"Eu tenho uma reserva.","de":"Ich habe eine Reservierung."},{"text":"Vocês têm toalhas?","de":"Habt ihr Handtücher?"},{"text":"Tem wi-fi no quarto?","de":"Gibt es WLAN im Zimmer?"}]},
 {"id":"precisar_de","title":"precisar de – brauchen",
  "explanation":"Nach precisar steht vor einem Nomen immer de. Mit Artikel verschmilzt es:\nde + o = do, de + a = da\n\n• Preciso de uma toalha. – Ich brauche ein Handtuch.\n• Preciso do passaporte. – Ich brauche den Pass.\n\nVor einem Verb fällt de weg: Preciso sair. – Ich muss gehen.",
  "examples":[{"text":"Preciso de ajuda.","de":"Ich brauche Hilfe."},{"text":"Preciso da chave.","de":"Ich brauche den Schlüssel."},{"text":"Preciso trocar de quarto.","de":"Ich muss das Zimmer wechseln."}]},
]
wg = {"onde":["fragewoerter"],"quando":["fragewoerter"],"qual":["fragewoerter"],"o_que":["fragewoerter"],"por_que":["fragewoerter"],"quem":["fragewoerter"],"ter":["ter_haben"],"precisar":["precisar_de"]}
make('04_hotel.json','pt-BR-04',4,'Hotel & Unterkunft','Hotel e hospedagem',groups,sentences,grammar,wg)
