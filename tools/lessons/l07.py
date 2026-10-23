from lib import make
groups = [
("Der Körper", """
corpo|o corpo|der Körper|n
cabeca|a cabeça|der Kopf|n
olho|o olho|das Auge|n
ouvido|o ouvido|das Ohr (innen)|n|Die Ohrmuschel: a orelha
nariz|o nariz|die Nase|n
boca|a boca|der Mund|n
dente|o dente|der Zahn|n
garganta|a garganta|der Hals (innen), die Kehle|n
costas|as costas|der Rücken|n|Immer Mehrzahl.
braco|o braço|der Arm|n
mao|a mão|die Hand|n|Weiblich, obwohl auf -o! Mehrzahl: as mãos
perna|a perna|das Bein|n
pe|o pé|der Fuss|n
barriga|a barriga|der Bauch|n
estomago|o estômago|der Magen|n
coracao|o coração|das Herz|n
pele|a pele|die Haut|n
"""),
("Beschwerden", """
dor|a dor|der Schmerz|n|dor de cabeça = Kopfschmerzen
febre|a febre|das Fieber|n
tosse|a tosse|der Husten|n
gripe|a gripe|die Grippe|n
resfriado|o resfriado|die Erkältung|n
diarreia|a diarreia|der Durchfall|n
alergia|a alergia|die Allergie|n
picada|a picada|der Stich (Insekt)|n
queimadura|a queimadura de sol|der Sonnenbrand|n
doente|doente|krank|adj
tonto|tonto|schwindlig|adj
enjoado|enjoado|übel, seekrank|adj
cansado|cansado|müde|adj
machucado|machucado|verletzt|adj
doer|doer|wehtun|v|Meist: dói (es tut weh), doem (sie tun weh)
"""),
("Arzt & Apotheke", """
medico|o médico|der Arzt|n|Die Ärztin: a médica
hospital|o hospital|das Krankenhaus|n
pronto_socorro|o pronto-socorro|die Notaufnahme|n
farmacia|a farmácia|die Apotheke|n
remedio|o remédio|das Medikament|n
comprimido|o comprimido|die Tablette|n
receita|a receita|das Rezept|n
consulta|a consulta|der Arzttermin, die Sprechstunde|n
seguro|o seguro de saúde|die Krankenversicherung|n
protetor|o protetor solar|die Sonnencreme|n
repelente|o repelente|das Mückenschutzmittel|n|Wichtig gegen Dengue-Mücken.
curativo|o curativo|das Pflaster; der Verband|n
"""),
("Notfall", """
socorro|socorro!|Hilfe!|int
ajuda|a ajuda|die Hilfe|n
emergencia|a emergência|der Notfall|n
ambulancia|a ambulância|der Krankenwagen|n
policia|a polícia|die Polizei|n
ladrao|o ladrão|der Dieb|n
roubar|roubar|stehlen, rauben|v
carteira|a carteira|das Portemonnaie|n
perdido|perdido|verloren; verirrt|adj
consulado|o consulado|das Konsulat|n
embaixada|a embaixada|die Botschaft|n
perigoso|perigoso|gefährlich|adj
urgente|urgente|dringend|adj
"""),
("Wörter & Verben", """
saude|a saúde|die Gesundheit|n|Saúde! = Gesundheit! (beim Niesen) und Prost!
melhor|melhor|besser|adj
pior|pior|schlechter|adj
chamar|chamar|rufen|v
ajudar|ajudar|helfen|v
tomar|tomar|einnehmen (Medikament); trinken|v
sentir|sentir|fühlen|v|Sinto-me mal / Estou me sentindo mal = Mir geht es schlecht.
ter_que|ter que|müssen|ph
"""),
]
sentences = [
("Socorro! Chame uma ambulância!","Hilfe! Rufen Sie einen Krankenwagen!","socorro ambulancia chamar","anweisungen"),
("É uma emergência!","Es ist ein Notfall!","emergencia"),
("Eu preciso de um médico.","Ich brauche einen Arzt.","eu medico precisar","precisar_de"),
("Onde fica o hospital mais perto?","Wo ist das nächste Krankenhaus?","onde hospital perto ficar"),
("Leve ele ao pronto-socorro.","Bring ihn in die Notaufnahme.","pronto_socorro levar","praep_artikel"),
("Tem uma farmácia aqui perto?","Gibt es hier in der Nähe eine Apotheke?","farmacia aqui perto","ter_haben"),
("Estou com dor de cabeça.","Ich habe Kopfschmerzen.","dor cabeca","doer estar_com"),
("Estou com dor de garganta e tosse.","Ich habe Halsschmerzen und Husten.","dor garganta tosse","doer estar_com"),
("Minhas costas doem muito.","Mein Rücken tut sehr weh.","costas doer muito","doer"),
("Dói aqui, na barriga.","Es tut hier weh, am Bauch.","doer aqui barriga","doer praep_artikel"),
("Meu estômago não está bem.","Mein Magen ist nicht in Ordnung.","estomago nao bem"),
("Acho que estou com febre.","Ich glaube, ich habe Fieber.","febre","estar_com"),
("Eu acho que é gripe.","Ich glaube, es ist eine Grippe.","eu gripe"),
("É só um resfriado.","Es ist nur eine Erkältung.","resfriado"),
("Estou com diarreia desde ontem.","Ich habe seit gestern Durchfall.","diarreia ontem","estar_com"),
("Tenho alergia a amendoim.","Ich habe eine Erdnussallergie.","alergia ter","ter_haben"),
("Eu tenho alergia a penicilina.","Ich bin allergisch gegen Penicillin.","eu alergia ter"),
("Tenho uma picada de mosquito no braço.","Ich habe einen Mückenstich am Arm.","picada braco ter","praep_artikel"),
("A picada está muito vermelha.","Der Stich ist sehr rot.","picada muito vermelho"),
("Fiquei com queimadura de sol na praia.","Ich habe am Strand einen Sonnenbrand bekommen.","queimadura praia"),
("Estou doente, não vou trabalhar hoje.","Ich bin krank, ich gehe heute nicht arbeiten.","doente nao hoje","ir_futuro"),
("Estou tonto e enjoado.","Mir ist schwindlig und übel.","tonto enjoado"),
("Estou muito cansado.","Ich bin sehr müde.","muito cansado"),
("Ele está machucado na perna.","Er ist am Bein verletzt.","machucado perna"),
("Machuquei o pé.","Ich habe mir den Fuss verletzt.","pe"),
("Quebrei o braço.","Ich habe mir den Arm gebrochen.","braco"),
("Cortei a mão com a faca.","Ich habe mich mit dem Messer in die Hand geschnitten.","mao faca com"),
("Meu olho está vermelho.","Mein Auge ist rot.","olho vermelho"),
("Estou com dor de ouvido.","Ich habe Ohrenschmerzen.","dor ouvido","estar_com"),
("O nariz está entupido.","Die Nase ist verstopft.","nariz"),
("Estou com dor de dente.","Ich habe Zahnschmerzen.","dor dente","estar_com"),
("Abra a boca, por favor.","Öffnen Sie bitte den Mund.","boca por_favor","anweisungen"),
("A pele está muito seca.","Die Haut ist sehr trocken.","pele muito"),
("Tenho um problema no coração.","Ich habe ein Problem mit dem Herzen.","problema coracao ter","praep_artikel"),
("O corpo todo dói.","Der ganze Körper tut weh.","corpo doer","doer"),
("Preciso marcar uma consulta.","Ich muss einen Arzttermin vereinbaren.","consulta precisar"),
("Precisa de receita para esse remédio?","Braucht man für dieses Medikament ein Rezept?","receita remedio esse","precisar_de demonstrativ"),
("Tome um comprimido três vezes por dia.","Nehmen Sie dreimal täglich eine Tablette.","comprimido num_3 tomar","anweisungen"),
("Você tem que tomar muita água.","Du musst viel Wasser trinken.","voce ter_que tomar agua","ter_que"),
("Eu tenho que ir ao médico.","Ich muss zum Arzt gehen.","eu ter_que medico ir","ter_que praep_artikel"),
("O meu seguro de saúde paga isso?","Bezahlt meine Krankenversicherung das?","seguro isso pagar"),
("Saúde é o mais importante.","Gesundheit ist das Wichtigste.","saude"),
("Você tem protetor solar?","Hast du Sonnencreme?","voce protetor ter"),
("Passe repelente à noite.","Trag abends Mückenschutz auf.","repelente noite","anweisungen"),
("Você tem um curativo?","Hast du ein Pflaster?","voce curativo ter"),
("Hoje eu me sinto melhor.","Heute fühle ich mich besser.","hoje eu melhor sentir"),
("Ontem estava pior.","Gestern war es schlimmer.","ontem pior"),
("Pode me ajudar, por favor?","Können Sie mir bitte helfen?","ajudar por_favor"),
("Obrigado pela ajuda!","Danke für die Hilfe!","obrigado ajuda","praep_artikel"),
("Chame a polícia!","Rufen Sie die Polizei!","chamar policia","anweisungen"),
("Pega ladrão!","Haltet den Dieb!","ladrao"),
("Roubaram a minha carteira.","Man hat mir mein Portemonnaie gestohlen.","roubar carteira"),
("Eu estou perdido.","Ich habe mich verirrt.","eu perdido"),
("Perdi o meu passaporte.","Ich habe meinen Pass verloren.","perder passaporte"),
("Onde fica o consulado da Alemanha?","Wo ist das deutsche Konsulat?","onde consulado ficar"),
("Preciso falar com a embaixada.","Ich muss mit der Botschaft sprechen.","precisar falar embaixada com"),
("Essa rua é perigosa à noite?","Ist diese Strasse nachts gefährlich?","esse rua perigoso noite"),
("É urgente!","Es ist dringend!","urgente"),
]
grammar = [
 {"id":"doer","title":"Schmerzen: dói, doem und estar com dor de",
  "explanation":"Im Portugiesischen ist der Körperteil das Subjekt – wie „es tut weh“:\n\n• Dói a cabeça. / A cabeça dói. – Der Kopf tut weh.\n• Doem as costas. – Der Rücken tut weh. (costas ist Mehrzahl)\n• Dói aqui. – Es tut hier weh.\n\nMeistens sagt man aber:\nestar com dor de … – Schmerzen haben in …\n• Estou com dor de cabeça. – Ich habe Kopfschmerzen.\n• Estou com dor de barriga. – Ich habe Bauchschmerzen.",
  "examples":[{"text":"Minha perna dói.","de":"Mein Bein tut weh."},{"text":"Estou com dor nas costas.","de":"Ich habe Rückenschmerzen."},{"text":"Onde dói?","de":"Wo tut es weh?"}]},
 {"id":"ter_que","title":"müssen: ter que und precisar",
  "explanation":"• ter que + Grundform – müssen (Pflicht)\n  Eu tenho que ir. – Ich muss gehen.\n  Você tem que descansar. – Du musst dich ausruhen.\n\n• precisar + Grundform – müssen, brauchen (Notwendigkeit)\n  Preciso dormir. – Ich muss schlafen.\n\n„nicht müssen“: não precisar – Você não precisa pagar. (Du musst nicht bezahlen.)\nAchtung: „não tem que“ klingt eher wie „du darfst nicht“.",
  "examples":[{"text":"Tenho que tomar o remédio.","de":"Ich muss das Medikament nehmen."},{"text":"Você precisa ir ao médico.","de":"Du musst zum Arzt."},{"text":"Não precisa, obrigado.","de":"Nicht nötig, danke."}]},
]
wg = {"doer":["doer"],"dor":["doer"],"ter_que":["ter_que"]}
make('07_gesundheit_notfall.json','pt-BR-07',7,'Gesundheit & Notfall','Saúde e emergências',groups,sentences,grammar,wg)
