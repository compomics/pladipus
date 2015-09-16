/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.enums;

/**
 *
 * @author Kenneth
 */
public enum AllowedMsConvertParams {

    //MANDATORY
    INPUT_FILE("f", "(mgf format,false), comma separated list or an entire folder.", true),
    OUTPUT_FOLDER("o", "(mgf format,false), comma separated list or an entire folder.", true),
    //formats
    MZML("-mzML", "write mzML format [default]", false),
    MZXML("-mzXML", "write mzXML format", false),
    MZ5("-mz5", "write mz5 format", false),
    MGF("-mgf", "write Mascot generic format", false),
    TEXT("-text", "write ProteoWizard internal text format", false),
    MS1("-ms1", "write MS1 format", false),
    CMS1("-cms1", "write CMS1 format", false),
    MS2("-ms2", "write MS2 format", false),
    CMS2("-cms2", "write CMS2 format", false),
    //FILTERS
    peakPicking("peakPicking", "This filter performs centroiding on spectrawith the selected MS levels, expressed as an int_set. The value for peak picker type must be \"cwt\" or \"vendor\": when <PickerType> = \"vendor\", vendor (Windows DLL) code is used if available. IMPORTANT NOTE: since this filter operates on the raw data through the vendor DLLs, it must be the first fileter in any list of filters when \"vendor\" is used. The other option for PickerType is \"cwt\", which uses ProteoWizard's wavelet-based algorithm for performing peak-picking with the specified wavelet-space signal-to-noise ratio.", false),
    index("index", "Selects spectra by index - an index value 0-based numerical order in which the spectrum appears in the input.", false),
    msLevel("msLevel", "This filter selects only spectra with the indicated MS levels, expressed as an int_set.", false),
    chargeState("chargeState ", "This filter keeps spectra that match the listed charge state(s,false), expressed as an int_set. Both known/single and possible/multiple charge states are tested. Use 0 to include spectra with no charge state at all.", false),
    precursorRecalculation("precursorRecalculation ", "This filter recalculates the precursor m/z and charge for MS2 spectra. It looks at the prior MS1 scan to better infer the parent mass. However, it only works on orbitrap and FT data,although it does not use any 3rd party (vendor DLL) code. Since the time the code was written, Thermo has since fixed up its own estimation in response, so it's less critical than it used to be (though can still be useful).", false),
    precursorRefine("precursorRefine", "This filter recalculates the precursor m/z and charge for MS2 spectra. It looks at the prior MS1 scan to better infer the parent mass. It only works on orbitrap, FT, and TOF data. It does not use any 3rd party (vendor DLL) code.", false),
    scanNumber("scanNumber", "This filter selects spectra by scan number. Depending on the input data type, scan number and spectrum index are not always the same thing - scan numbers are not always contiguous, and are usually 1-based.", false),
    scanEvent("scanEvent", "This filter selects spectra by scan event. For example, to include all scan events except scan event 5, use filter \"scanEvent 1-4 6-\". A \"scan event\" is a preset scan configuration: a user-defined scan configuration that specifies the instrumental settings in which a spectrum is acquired. An instrument may cycle through a list of preset scan configurations to acquire data. This is a more generic term for the Thermo \"scan event\", which is defined in the Thermo Xcalibur glossary as: \"a mass spectrometer scan that is defined by choosing the necessary scan parameter settings. Multiple scan events can be defined for each segment of time.\".", false),
    scanTime("scanTime", "This filter selects only spectra within a given time range (in seconds).", false),
    scanSumming("scanSumming", "This filter sums MS2 sub-scans whose precursors are within precursor tolerance (default: 0.05 m/z) and scan time tolerance (default: 10 secs.). It is intended for some Waters DDA data, where sub-scans should be summed together to increase the SNR. This filter has only been tested for Waters data.", false),
    sortByScanType("sortByScanType", "This filter reorders spectra, sorting them by ascending scan start time.", false),
    stripIT("stripIT", "This filter rejects ion trap data spectra with MS level 1.", false),
    metaDataFixer("metaDataFixer", "This filter is used to add or replace a spectra's TIC/BPI metadata, usually after peakPicking where the change from profile to centroided data may make the TIC and BPI values inconsistent with the revised scan data. The filter traverses the m/z intensity arrays to find the sum and max. For example, in msconvert it can be used as: --filter \"peakPicking true 1-\" --filter metadataFixer. It can also be used without peak picking and is provided without guarantee on the results correctness.", false),
    titleMaker("titleMaker", "This filter adds or replaces spectrum titles according to specified arguments. It can be used, for example, to customize the TITLE line in MGF output in msconvert.", false),
    threshold("threshold", "This filter keeps data whose values meet various threshold criteria.", false),
    mzWindow("mzWindow", "keeps mz/intensity pairs whose m/z values fall within the specified range.", false),
    mzPrecursors("mzPrecursors", "Retains spectra with precursor m/z values found in the given list. For example, in msconvert to retain only spectra with precursor m/z values of 123.4 and 567.8 you would use \"[123.4,567.8]\". Note that this filter will drop MS1 scans unless you include 0.0 in the list of precursor values.", false),
    defaultArrayLength("defaultArrayLength", "Keeps only spectra with peak counts within <peak_count_range>, expressed as an int_set. (In mzML the peak list length is expressed as \"defaultArrayLength\", hence the name.) For example, to include only spectra with 100 or more peaks, you would use \"defaultArrayLength 100-\" .", false),
    zeroSamples("zeroSamples", "This filter deals with zero values in spectra - either removing them, or adding them where they are missing.", false),
    mzPresent("mzPresent", "This filter is similar to the \"threshold\" filter, with a few more options.", false),
    MS2Denoise("MS2Denoise", "Noise peak removal for spectra with precursor ions.", false),
    MS2Deisotope("MS2Deisotope", "Deisotopes ms2 spectra using the Markey method or a Poisson model. For the Markey method, hi_res sets high resolution mode to \"false\" (the default) or \"true\". Poisson activates a Poisson model based on the relative intensity distribution.", false),
    turbocharger("turbocharger", "Predicts MSn spectrum precursor charge based on the isotopic distribution associated with the survey scan(s) of the selected precursor.", false),
    ETDFilter("ETDFilter", "Filters ETD MSn spectrum data points, removing unreacted precursors, charge-reduced precursors, and neutral losses.", false),
    chargeStatePredictor("chargeStatePredictor", "Predicts MSn spectrum precursors to be singly or multiply charged depending on the ratio of intensity above and below the precursor m/z, or optionally using the \"makeMS2\" algorithm.", false),
    activation("activation", "Keeps only spectra whose precursors have the specifed activation type. It does not affect non-MS spectra, and does not affect MS1 spectra. Use it to create output files containing only ETD or CID/HCD MSn data where both activation modes have been interleaved within a given input vendor data file (eg: Thermo's Decision Tree acquisition mode).", false),
    analyzer("analyzer", "This filter keeps only spectra with the indicated mass analyzer type.", false),
    polarity("polarity", "Keeps only spectra with scan of the selected polarity.", false);

    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private AllowedMsConvertParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

}
