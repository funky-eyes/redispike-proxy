function write_bin_if_not_exists(rec, bin_name, bin_value)
    if aerospike:exists(rec) then
        if rec[bin_name] ~= nil then
            return "0"
        else
            rec[bin_name] = bin_value
            aerospike:update(rec)
            return "1"
        end
    else
        rec[bin_name] = bin_value
        aerospike:create(rec)
        return "1"
    end
end