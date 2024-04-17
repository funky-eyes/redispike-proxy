function hash_count_bins(rec)
    local bins = record.bin_names(rec)
    local bin_count = #bins
    return bin_count
end