package edu.fiu.cate.nomad.audio.nlp;

import java.util.EnumSet;
import java.util.TreeMap;

public enum PennPartOfSpeech {
	
	CC("Coordinating conjunction"),
	CD("Cardinal number"),
	DT("Determiner"),
	EX("Existential there"),
	FW("Foreign word"),
	IN("Preposition or subordinating conjunction"),
	JJ("Adjective"),
	JJR("Adjective, comparative"),
	JJS("Adjective, superlative"),
	LS("List item marker"),
	MD("Modal"),
	NN("Noun, singular or mass"),
	NNS("Noun, plural"),
	NNP("Proper noun, singular"),
	NNPS("Proper noun, plural"),
	PDT("Predeterminer"),
	POS("Possessive ending"),
	PRP("Personal pronoun"),
	PRP$("Possessive pronoun"),
	RB("Adverb"),
	RBR("Adverb, comparative"),
	RBS("Adverb, superlative"),
	RP("Particle"),
	SYM("Symbol"),
	TO("to"),
	UH("Interjection"),
	VB("Verb, base form"),
	VBD("Verb, past tense"),
	VBG("Verb, gerund or present participle"),
	VBN("Verb, past participle"),
	VBP("Verb, non-3rd person singular present"),
	VBZ("Verb, 3rd person singular present"),
	WDT("Wh-determiner"),
	WP("Wh-pronoun"),
	WP$("Possessive wh-pronoun"),
	WRB("Wh-adverb");
	
	final String description;

	final static TreeMap<String, PennPartOfSpeech> name2enum = new TreeMap<>();
	static{
		for(PennPartOfSpeech e: EnumSet.allOf(PennPartOfSpeech.class)){
			name2enum.put(e.name(), e);			
		}
	}
	
	PennPartOfSpeech(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static PennPartOfSpeech getEnum(String rep){
		return name2enum.get(rep);
	}
	
	public static String getDescription(String rep){
		PennPartOfSpeech e = name2enum.get(rep);
		if(e==null) return "No valid description avaliable.";
		return e.description;
	}

}
